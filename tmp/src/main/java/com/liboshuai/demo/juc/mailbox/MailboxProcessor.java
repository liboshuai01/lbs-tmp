package com.liboshuai.demo.juc.mailbox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailboxProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MailboxProcessor.class);

    private final TaskMailbox mailbox;
    private final MailboxDefaultAction mailboxDefaultAction;
    private final MailboxExecutorImpl mainThreadExecutor;

    // 控制循环的标志位
    private boolean isRunning = true;
    private boolean isDefaultActionFinished = false;

    public MailboxProcessor(MailboxDefaultAction defaultAction) {
        this.mailboxDefaultAction = defaultAction;
        this.mailbox = new TaskMailboxImpl();
        this.mainThreadExecutor = new MailboxExecutorImpl(mailbox, 0);
    }

    public MailboxExecutorImpl getMainThreadExecutor() {
        return mainThreadExecutor;
    }

    /**
     * 核心循环：交替处理 邮件 和 数据流
     */
    public void runMailboxLoop() throws Exception {
        final MailboxDefaultAction.Controller controller = new MailboxDefaultAction.Controller() {
            @Override
            public void allFinished() {
                isDefaultActionFinished = true;
            }
        };

        while (isRunning) {
            // 1. 优先处理所有等待中的邮件 (Checkpoint, RPC回调等)
            // 类似于源码中的 processMail(mailbox, true)
            while (mailbox.hasMail()) {
                Runnable mail = mailbox.tryTake(0);
                if (mail != null) {
                    mail.run();
                }
            }

            // 2. 如果数据流处理完了，只剩下处理邮件并等待关闭
            if (isDefaultActionFinished) {
                // 阻塞等待新邮件，不再跑 defaultAction
                Runnable mail = mailbox.take(0);
                mail.run();
                continue;
            }

            // 3. 执行默认动作 (处理一条数据)
            // 源码：if (!mailbox.hasMail()) { defaultAction.runDefaultAction(controller); }
            mailboxDefaultAction.runDefaultAction(controller);
        }
    }

    public void stop() {
        // 实际上这里会发一个特殊的 Poison Pill 邮件或者设置标志位
        isRunning = false;
        // 确保如果线程阻塞在 take() 上能被唤醒（这里简化处理，直接投递一个空包）
        mailbox.put(() -> {});
    }

    public void close() {
        mailbox.drain();
    }
}
