package com.liboshuai.demo.juc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MailboxDemo {

    // 模拟 Flink 中的 State (例如 ValueState)
    static int sharedState = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. 传统多线程锁模型 (模拟) ===");
        runLockModel();

        System.out.println("\n=== 2. Flink Mailbox 模型 (模拟) ===");
        runMailboxModel();
    }

    // --- 模型 1: 传统锁并发 ---
    private static void runLockModel() throws InterruptedException {
        sharedState = 0;
        int threadCount = 10;
        int updatesPerThread = 1000;
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Object lock = new Object(); // 必须用锁

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            threadPool.submit(() -> {
                for (int j = 0; j < updatesPerThread; j++) {
                    // 必须加锁，否则数据不一致
                    synchronized (lock) {
                        sharedState++;
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        threadPool.shutdown();
        System.out.println("最终状态值: " + sharedState + ", 耗时: " + (end - start) + "ms (存在锁竞争)");
    }

    // --- 模型 2: Mailbox 模型 ---
    private static void runMailboxModel() throws InterruptedException {
        sharedState = 0;
        int threadCount = 10;
        int updatesPerThread = 1000;
        ExecutorService producers = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 1. 启动 MailboxProcessor (模拟 Flink Task 线程)
        // 这里的 mainAction 传 null，专心处理邮箱消息
        MailboxProcessor processor = new MailboxProcessor(null);
        Thread taskThread = new Thread(processor, "Flink-Task-Thread");
        taskThread.start();

        long start = System.currentTimeMillis();

        // 2. 模拟外部线程 (如 RPC, Checkpoint Trigger)
        for (int i = 0; i < threadCount; i++) {
            producers.submit(() -> {
                try {
                    for (int j = 0; j < updatesPerThread; j++) {
                        // 关键区别：不直接修改 sharedState，而是投递一个动作
                        processor.getMailbox().put(() -> {
                            // 这段代码将在 "Flink-Task-Thread" 中执行
                            // 绝对线程安全，不需要 synchronized
                            sharedState++;
                        });
                    }
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.await();

        // 稍微等待一下，确保消费者处理完队列里的所有消息（Demo代码简化处理）
        Thread.sleep(500);

        // 3. 验证结果
        // 我们可以向邮箱投递一个打印结果的任务，来查看最终一致性
        processor.getMailbox().put(() -> {
            long end = System.currentTimeMillis();
            System.out.println("最终状态值: " + sharedState + ", 耗时(外部视角): " + (end - start) + "ms (无锁)");
            processor.stop(); // 停止处理循环
        });

        producers.shutdown();
        taskThread.join();
    }
}
