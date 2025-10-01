package com.liboshuai.demo.threadpool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LbsThreadPoolExecutor 类的单元测试。
 */
class LbsThreadPoolExecutorTest {

    @Test
    @DisplayName("测试：基本的任务执行与完成")
    void testTaskExecution() throws InterruptedException {
        // 创建一个线程池
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                2, 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new LbsAbortPolicy()
        );

        final int taskCount = 5;
        final CountDownLatch latch = new CountDownLatch(taskCount);
        final AtomicInteger executedTasks = new AtomicInteger(0);

        // 提交5个任务
        for (int i = 0; i < taskCount; i++) {
            executor.execute(() -> {
                executedTasks.incrementAndGet(); // 增加已执行任务计数
                latch.countDown(); // 倒数
            });
        }

        // 等待所有任务完成，设置5秒超时
        assertTrue(latch.await(5, TimeUnit.SECONDS), "所有任务应该在超时时间内完成");
        assertEquals(taskCount, executedTasks.get(), "所有提交的任务都应该被执行");

        executor.shutdown();
    }

    @Test
    @DisplayName("测试：当队列满时，线程数能从核心线程数增长到最大线程数")
    void testCoreAndMaximumPoolSize() throws InterruptedException {
        final int corePoolSize = 1;
        final int maxPoolSize = 2;
        final int queueCapacity = 1;
        final int totalTasks = 3; // 1个给核心线程 + 1个进队列 + 1个创建新(非核心)线程

        final CountDownLatch finishLatch = new CountDownLatch(totalTasks);
        final AtomicInteger threadsCreated = new AtomicInteger(0);

        // 自定义线程工厂，用于追踪创建的线程数
        ThreadFactory threadFactory = r -> {
            threadsCreated.incrementAndGet();
            return new Thread(r);
        };

        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), threadFactory, new LbsAbortPolicy()
        );

        // 提交3个任务，每个任务休眠一小段时间以确保线程被占用
        for (int i = 0; i < totalTasks; i++) {
            executor.execute(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finishLatch.countDown();
            });
        }

        assertTrue(finishLatch.await(5, TimeUnit.SECONDS), "所有任务都应该完成");
        // 第1个任务 -> 创建核心线程。第2个 -> 进入队列。第3个 -> 创建非核心线程。
        assertEquals(maxPoolSize, threadsCreated.get(), "线程池应该创建达到最大数量的线程");

        executor.shutdown();
    }

    @Test
    @DisplayName("测试：空闲的非核心线程在 keepAliveTime 后应该被终止")
    void testKeepAliveTime() throws InterruptedException {
        final AtomicInteger threadsCreated = new AtomicInteger(0);
        final ThreadFactory threadFactory = r -> {
            threadsCreated.incrementAndGet();
            return new Thread(r);
        };

        // 创建一个没有核心线程的线程池，keepAliveTime很短
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                0, 1, 50, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory, new LbsAbortPolicy()
        );

        final CountDownLatch task1Latch = new CountDownLatch(1);
        // 提交任务1，创建一个线程
        executor.execute(task1Latch::countDown);
        assertTrue(task1Latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, threadsCreated.get(), "第一个任务应该创建一个线程");

        // 等待超过 keepAliveTime，让空闲线程消亡
        Thread.sleep(100);

        final CountDownLatch task2Latch = new CountDownLatch(1);
        // 提交任务2，此时应该会创建一个新线程
        executor.execute(task2Latch::countDown);
        assertTrue(task2Latch.await(1, TimeUnit.SECONDS));
        assertEquals(2, threadsCreated.get(), "旧线程超时后，新任务应该创建一个新线程");

        executor.shutdown();
    }

    @Test
    @DisplayName("测试：shutdown() 方法的行为")
    void testShutdown() throws InterruptedException {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5), new LbsAbortPolicy()
        );

        final CountDownLatch runningTaskLatch = new CountDownLatch(1);
        final AtomicInteger completedTasks = new AtomicInteger(0);

        // 一个正在运行的任务
        executor.execute(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            completedTasks.incrementAndGet();
            runningTaskLatch.countDown();
        });

        // 几个在队列中等待的任务
        for (int i = 0; i < 3; i++) {
            executor.execute(completedTasks::incrementAndGet);
        }

        assertFalse(executor.isShutdown());
        executor.shutdown(); // 发起关闭
        assertTrue(executor.isShutdown());

        // 关闭后，新任务应该被拒绝
        assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> fail("这个任务应该被拒绝。")));

        // 等待正在运行和队列中的任务完成
        assertTrue(runningTaskLatch.await(2, TimeUnit.SECONDS));
        // 等待队列任务执行
        Thread.sleep(500);

        assertEquals(4, completedTasks.get(), "所有在shutdown之前提交的任务都应该完成");
    }

    @Test
    @DisplayName("测试：shutdownNow() 方法的行为")
    void testShutdownNow() throws InterruptedException {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5), new LbsAbortPolicy()
        );

        final AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        final CountDownLatch taskStarted = new CountDownLatch(1);

        // 这个任务将被中断
        executor.execute(() -> {
            taskStarted.countDown();
            try {
                Thread.sleep(5000); // 长时间休眠，确保能被中断
            } catch (InterruptedException e) {
                wasInterrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });

        // 这两个任务将在队列中，并被返回
        Runnable queuedTask1 = () -> System.out.println("队列任务1");
        Runnable queuedTask2 = () -> System.out.println("队列任务2");
        executor.execute(queuedTask1);
        executor.execute(queuedTask2);

        // 确保第一个任务已经开始执行
        assertTrue(taskStarted.await(1, TimeUnit.SECONDS));

        List<Runnable> unexecutedTasks = executor.shutdownNow();

        assertTrue(executor.isShutdown(), "线程池应该处于关闭状态");
        assertEquals(2, unexecutedTasks.size(), "shutdownNow应该返回队列中未执行的任务");
        assertTrue(unexecutedTasks.contains(queuedTask1));
        assertTrue(unexecutedTasks.contains(queuedTask2));

        // 等待一小会儿，让中断信号被处理
        Thread.sleep(100);
        assertTrue(wasInterrupted.get(), "正在运行的任务应该被中断");
    }

    @Test
    @DisplayName("测试拒绝策略：AbortPolicy (抛出异常)")
    @Timeout(2)
    void testAbortPolicy() {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1), new LbsAbortPolicy()
        );

        // 任务1：占用唯一的线程
        executor.execute(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        // 任务2：占满队列
        executor.execute(() -> {});

        // 任务3：此时线程和队列都满了，应该被拒绝
        assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> System.out.println("我应该被拒绝。")));

        executor.shutdown();
    }

    @Test
    @DisplayName("测试拒绝策略：CallerRunsPolicy (由提交者线程执行)")
    void testCallerRunsPolicy() throws InterruptedException {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1), new LbsCallerRunsPolicy()
        );

        final String mainThreadName = Thread.currentThread().getName();
        final AtomicBoolean rejectedTaskRun = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        // 任务1：占用线程
        executor.execute(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        // 任务2：占满队列
        executor.execute(() -> {});

        // 任务3：被拒绝后，应该由主线程（调用者）执行
        executor.execute(() -> {
            assertEquals(mainThreadName, Thread.currentThread().getName(), "任务应该在主线程中执行");
            rejectedTaskRun.set(true);
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS), "被拒绝的任务应该立即执行");
        assertTrue(rejectedTaskRun.get());

        executor.shutdownNow();
    }

    @Test
    @DisplayName("测试拒绝策略：DiscardPolicy (直接丢弃)")
    void testDiscardPolicy() throws InterruptedException {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1), new LbsDiscardPolicy()
        );

        final CountDownLatch latch = new CountDownLatch(2); // 预期只有2个任务会运行
        final AtomicInteger tasksExecuted = new AtomicInteger(0);

        // 任务1：占用线程
        executor.execute(() -> {
            tasksExecuted.incrementAndGet();
            latch.countDown();
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        // 任务2：占满队列
        executor.execute(() -> {
            tasksExecuted.incrementAndGet();
            latch.countDown();
        });

        // 任务3：应该被静默地丢弃
        executor.execute(() -> fail("这个被丢弃的任务永远不应该被执行。"));

        assertTrue(latch.await(2, TimeUnit.SECONDS), "前两个任务应该被执行");
        assertEquals(2, tasksExecuted.get(), "总共只应该执行2个任务");

        executor.shutdown();
    }

    @Test
    @DisplayName("测试拒绝策略：DiscardOldestPolicy (丢弃队列中最旧的任务)")
    void testDiscardOldestPolicy() throws InterruptedException {
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1), new LbsDiscardOldestPolicy()
        );

        final CountDownLatch latch = new CountDownLatch(2);
        // 使用一个并发队列来记录实际执行的任务的ID
        final ConcurrentLinkedQueue<Integer> executedTasks = new ConcurrentLinkedQueue<>();

        // 任务1：占用线程
        executor.execute(() -> {
            executedTasks.add(1);
            latch.countDown();
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        // 任务2：进入队列，这个任务预期将被丢弃
        executor.execute(() -> {
            executedTasks.add(2);
            latch.countDown();
        });

        // 任务3：触发拒绝策略，它会把任务2从队列中移除，并把自己加进去
        executor.execute(() -> {
            executedTasks.add(3);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "应该有两个任务完成");
        // 最终执行的应该是任务1和任务3
        assertEquals(2, executedTasks.size());
        assertTrue(executedTasks.contains(1), "任务1应该被执行");
        assertFalse(executedTasks.contains(2), "任务2（最旧的）应该被丢弃");
        assertTrue(executedTasks.contains(3), "任务3（新来的）应该被执行");

        executor.shutdown();
    }

    @Test
    @DisplayName("测试：构造函数参数校验")
    void testConstructorArgumentValidation() {
        // 测试非法参数
        assertThrows(IllegalArgumentException.class, () -> new LbsThreadPoolExecutor(-1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new LbsAbortPolicy()));
        assertThrows(IllegalArgumentException.class, () -> new LbsThreadPoolExecutor(1, 0, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new LbsAbortPolicy()));
        assertThrows(IllegalArgumentException.class, () -> new LbsThreadPoolExecutor(2, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new LbsAbortPolicy()));
        assertThrows(IllegalArgumentException.class, () -> new LbsThreadPoolExecutor(1, 1, -1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new LbsAbortPolicy()));

        // 测试空指针
        assertThrows(NullPointerException.class, () -> new LbsThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, null, new LbsAbortPolicy()));
        assertThrows(NullPointerException.class, () -> new LbsThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), null));
        assertThrows(NullPointerException.class, () -> new LbsThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), null, new LbsAbortPolicy()));
    }
}