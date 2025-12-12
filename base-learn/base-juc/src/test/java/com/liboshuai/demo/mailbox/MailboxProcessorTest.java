package com.liboshuai.demo.mailbox;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MailboxProcessorTest {

    private TaskMailboxImpl mailbox;
    private List<String> executionTrace;

    // 用于测试跳出死循环的异常
    private static class BreakLoopException extends RuntimeException {}

    @Before
    public void setUp() {
        mailbox = new TaskMailboxImpl(Thread.currentThread());
        executionTrace = new ArrayList<>();
    }

    @After
    public void tearDown() {
        mailbox.close();
    }

    @Test
    public void testPriorityExecution() {
        // 场景：先有 Mail，再执行 DefaultAction
        // 我们预先填入几个 Mail
        mailbox.put(new Mail(() -> executionTrace.add("Mail1"), 0, "m1"));
        mailbox.put(new Mail(() -> executionTrace.add("Mail2"), 0, "m2"));

        // 定义 DefaultAction
        MailboxDefaultAction defaultAction = controller -> {
            executionTrace.add("DefaultAction");
            // 执行一次后抛异常退出循环，否则测试无法结束
            throw new BreakLoopException();
        };

        MailboxProcessor processor = new MailboxProcessor(defaultAction, mailbox);

        try {
            processor.runMailboxLoop();
        } catch (BreakLoopException e) {
            // Expected
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        // 验证执行顺序：必须先清空邮箱，才执行 DefaultAction
        Assert.assertEquals(3, executionTrace.size());
        Assert.assertEquals("Mail1", executionTrace.get(0));
        Assert.assertEquals("Mail2", executionTrace.get(1));
        Assert.assertEquals("DefaultAction", executionTrace.get(2));
    }

    @Test
    public void testSuspendAndResume() throws Exception {
        // 场景：DefaultAction 请求挂起 -> 验证不再被调用 -> 外部发送 Resume Mail -> 恢复调用

        AtomicInteger actionCount = new AtomicInteger(0);

        // 这是一个有状态的 DefaultAction
        MailboxDefaultAction defaultAction = new MailboxDefaultAction() {
            @Override
            public void runDefaultAction(Controller controller) {
                int count = actionCount.incrementAndGet();
                executionTrace.add("Action-" + count);

                if (count == 1) {
                    // 第一次运行：请求挂起
                    executionTrace.add("Suspending");
                    controller.suspendDefaultAction();

                    // 模拟外部线程：往邮箱里发一个“唤醒任务”
                    // 注意：真实场景是 I/O 线程调用的，这里我们直接往邮箱塞一个特殊的 Mail
                    // 这个 Mail 的作用是模拟 resume()
                    mailbox.put(new Mail(() -> {
                        executionTrace.add("Processing-Resume-Mail");
                        // 拿到 processor 引用调用 resume (这里简化测试，手动模拟 resume 效果)
                        // 但因为 processor 是局部变量，我们在测试里只要验证 "take()" 被唤醒即可。
                        // 真正的 resume 需要通过 processor.resumeDefaultAction()。
                        // 我们用一种 tricky 的方式：在这个 Mail 里抛异常退出，
                        // 这样我们可以断言在此之前发生了什么。
                        throw new BreakLoopException();
                    }, 0, "Resume"));
                }
            }
        };

        MailboxProcessor processor = new MailboxProcessor(defaultAction, mailbox);

        try {
            processor.runMailboxLoop();
        } catch (BreakLoopException e) {
            // Loop ended by our Resume Mail
        }

        // 验证痕迹
        // 1. Action-1 执行
        // 2. Suspending 调用
        // 3. (关键) 此时 isDefaultActionAvailable = false，循环应该进入 mailbox.take() 阻塞
        // 4. 我们 put 的 "Resume" 邮件唤醒了 take()
        // 5. "Processing-Resume-Mail" 执行

        Assert.assertEquals(3, executionTrace.size());
        Assert.assertEquals("Action-1", executionTrace.get(0));
        Assert.assertEquals("Suspending", executionTrace.get(1));
        Assert.assertEquals("Processing-Resume-Mail", executionTrace.get(2));

        // 注意：如果不发送那个 Mail，程序会一直卡在 take() 导致测试超时，
        // 这也反向证明了 suspend 是生效的（它确实阻塞了，没有死循环调用 DefaultAction）。
    }
}
