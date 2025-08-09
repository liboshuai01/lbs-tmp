package com.liboshuai.demo.thread.readwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 3: 演示锁降级过程。
 * 场景：一个线程需要更新数据，并且在更新后需要立即读取该数据，
 * 同时不希望在读取过程中数据被其他写线程修改，但可以允许其他读线程进入。
 */
public class LockDowngradingDemo {
    private int data = 0;
    private volatile boolean dataChanged = false; // 使用 volatile 保证可见性
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public void processData() {
        // 步骤 1: 获取写锁，进行数据修改
        writeLock.lock();
        System.out.println(Thread.currentThread().getName() + " 获取了写锁，准备修改数据。");
        try {
            data++;
            dataChanged = true;
            System.out.println(Thread.currentThread().getName() + " 数据修改完成。");

            // 步骤 2: 在释放写锁之前，获取读锁（实现锁降级）
            // 目的是为了保证当前线程能看到刚刚修改的数据，并且在读取期间，其他写线程不能进入。
            readLock.lock();
            System.out.println(Thread.currentThread().getName() + " 在持有写锁的情况下，获取了读锁（锁降级）。");

        } finally {
            // 步骤 3: 释放写锁。此时当前线程仍然持有读锁。
            writeLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放了写锁。");
        }

        // 步骤 4: 在只持有读锁的情况下，进行数据读取和处理
        // 此刻，其他读线程可以进入，但写线程会被阻塞。
        readLock.lock(); // 这里只是为了演示，实际上上面已经获取了，这里会重入
        try {
            if (dataChanged) {
                System.out.println(Thread.currentThread().getName() + " 持有读锁，读取到更新后的 data: " + data);
                // 使用完数据后，重置标志位
                dataChanged = false; // 注意：在读锁中修改状态通常不是好主意，这里仅为演示流程。
                // 更好的做法是在写锁中完成所有修改。
            }
        } finally {
            readLock.unlock();
            readLock.unlock(); // 释放两次读锁
            System.out.println(Thread.currentThread().getName() + " 释放了读锁。");
        }
    }

    public static void main(String[] args) {
        LockDowngradingDemo demo = new LockDowngradingDemo();
        new Thread(demo::processData, "Downgrading-Thread").start();
    }
}