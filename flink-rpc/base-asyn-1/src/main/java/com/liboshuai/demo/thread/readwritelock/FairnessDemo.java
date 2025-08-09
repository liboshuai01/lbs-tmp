package com.liboshuai.demo.thread.readwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 5: 对比公平锁和非公平锁。
 * 公平锁：等待时间最长的线程优先获取锁（FIFO）。
 * 非公平锁（默认）：允许新请求插队，可能导致等待中的线程饥饿。
 */
public class FairnessDemo {

    public static void main(String[] args) throws InterruptedException {
        // 使用非公平锁进行测试
        System.out.println("--- 非公平锁测试 (Non-fair Lock Test) ---");
        runTest(new ReentrantReadWriteLock(false)); // false 表示非公平

        Thread.sleep(3000); // 等待上个测试结束

        // 使用公平锁进行测试
        System.out.println("\n--- 公平锁测试 (Fair Lock Test) ---");
        runTest(new ReentrantReadWriteLock(true)); // true 表示公平
    }

    private static void runTest(final ReentrantReadWriteLock lock) {
        // 创建一个写线程
        Thread writer = new Thread(() -> {
            lock.writeLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " acquired write lock.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            } finally {
                System.out.println(Thread.currentThread().getName() + " released write lock.");
                lock.writeLock().unlock();
            }
        }, "Writer");

        // 创建多个读线程
        Runnable readTask = () -> {
            lock.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " acquired read lock.");
            } finally {
                lock.readLock().unlock();
            }
        };

        // 启动顺序：先启动写线程，然后立即启动多个读线程
        writer.start();
        try {
            Thread.sleep(50); // 确保写线程先启动并尝试获取锁
        } catch (InterruptedException ignored) {}

        for (int i = 0; i < 5; i++) {
            new Thread(readTask, "Reader-" + i).start();
        }

        // 在读线程后面，再启动一个写线程
        new Thread(writer, "Writer-Followup").start();
    }
}