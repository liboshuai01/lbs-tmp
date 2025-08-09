package com.liboshuai.demo.thread.readwritelock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 2: 演示读写锁的可重入性。
 */
public class ReentrancyDemo {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public void readReentrant() {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 第一次获取读锁，读锁计数: " + rwLock.getReadHoldCount());
            // 再次获取读锁
            readLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 第二次获取读锁，读锁计数: " + rwLock.getReadHoldCount());
            } finally {
                readLock.unlock();
                System.out.println(Thread.currentThread().getName() + " 释放一次读锁，读锁计数: " + rwLock.getReadHoldCount());
            }
        } finally {
            readLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放最后一次读锁，读锁计数: " + rwLock.getReadHoldCount());
        }
    }

    public void writeReentrant() {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 第一次获取写锁，写锁计数: " + rwLock.getWriteHoldCount());
            // 再次获取写锁
            writeLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 第二次获取写锁，写锁计数: " + rwLock.getWriteHoldCount());
            } finally {
                writeLock.unlock();
                System.out.println(Thread.currentThread().getName() + " 释放一次写锁，写锁计数: " + rwLock.getWriteHoldCount());
            }
        } finally {
            writeLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放最后一次写锁，写锁计数: " + rwLock.getWriteHoldCount());
        }
    }

    // 演示持有写锁时，可以获取读锁
    public void writeThenRead() {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 持有写锁...");
            // 在持有写锁的情况下，获取读锁
            readLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 在写锁内部，成功获取读锁！");
            } finally {
                readLock.unlock();
                System.out.println(Thread.currentThread().getName() + " 释放了内部的读锁。");
            }
        } finally {
            writeLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放了外部的写锁。");
        }
    }

    public static void main(String[] args) {
        ReentrancyDemo demo = new ReentrancyDemo();
        new Thread(demo::readReentrant, "Read-Reentrant-Thread").start();
        new Thread(demo::writeReentrant, "Write-Reentrant-Thread").start();
        new Thread(demo::writeThenRead, "Write-Then-Read-Thread").start();
    }
}