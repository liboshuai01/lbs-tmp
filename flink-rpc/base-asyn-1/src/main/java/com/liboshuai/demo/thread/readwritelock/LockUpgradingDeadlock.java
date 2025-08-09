package com.liboshuai.demo.thread.readwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 4: 演示锁升级导致的死锁。这是一个错误用法的示例。
 * ReentrantReadWriteLock 不支持锁升级。
 */
public class LockUpgradingDeadlock {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public void performTask() {
        System.out.println(Thread.currentThread().getName() + " 准备获取读锁...");
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 已获取读锁。");

            // 关键点：在持有读锁的情况下，尝试获取写锁（锁升级）
            // 这个操作会导致当前线程永久阻塞，如果其他线程也这么做，就会死锁。
            System.out.println(Thread.currentThread().getName() + " 准备获取写锁（尝试锁升级）...");
            writeLock.lock();
            System.out.println(Thread.currentThread().getName() + " !!!永远不会执行到这里!!!");
            writeLock.unlock();

        } finally {
            readLock.unlock();
        }
    }

    public static void main(String[] args) {
        LockUpgradingDeadlock demo = new LockUpgradingDeadlock();

        // 启动两个线程，它们都会先获取读锁，然后尝试升级为写锁
        new Thread(demo::performTask, "Thread-A").start();
        new Thread(demo::performTask, "Thread-B").start();

        // 正确的做法是：先释放读锁，再去竞争写锁
        // new Thread(() -> {
        //     readLock.lock();
        //     // ... do something ...
        //     readLock.unlock(); // 先释放
        //     writeLock.lock(); // 再获取
        //     // ... do something ...
        //     writeLock.unlock();
        // }).start();
    }
}