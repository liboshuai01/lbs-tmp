package com.liboshuai.demo.thread.readwritelock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 1: 演示读写锁的基本用法，模拟一个简单的缓存实现。
 * 目标：允许多个线程同时读取缓存，但在写入缓存时，所有其他读写操作都必须等待。
 */
public class BasicUsageCache {

    private final Map<String, Object> cache = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    /**
     * 读取缓存的方法
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        readLock.lock(); // 获取读锁
        try {
            System.out.println(Thread.currentThread().getName() + " 正在读取 key: " + key);
            // 模拟读取耗时
            TimeUnit.MILLISECONDS.sleep(500);
            return cache.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            System.out.println(Thread.currentThread().getName() + " 读取完成。");
            readLock.unlock(); // 释放读锁
        }
    }

    /**
     * 写入缓存的方法
     * @param key 键
     * @param value 值
     */
    public void put(String key, Object value) {
        writeLock.lock(); // 获取写锁
        try {
            System.out.println(Thread.currentThread().getName() + " 正在写入 key: " + key + ", value: " + value);
            // 模拟写入耗时
            TimeUnit.MILLISECONDS.sleep(500);
            cache.put(key, value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(Thread.currentThread().getName() + " 写入完成。");
            writeLock.unlock(); // 释放写锁
        }
    }

    public static void main(String[] args) {
        BasicUsageCache cache = new BasicUsageCache();

        // 启动一个写线程
        new Thread(() -> {
            cache.put("config", "version-1.0");
        }, "Writer-1").start();

        // 启动多个读线程
        for (int i = 0; i < 5; i++) {
            final int temp = i;
            new Thread(() -> {
                cache.get("config");
            }, "Reader-" + temp).start();
        }

        // 再次启动一个写线程，观察它如何等待所有读线程结束
        new Thread(() -> {
            cache.put("config", "version-2.0");
        }, "Writer-2").start();
    }
}