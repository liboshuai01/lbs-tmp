package com.liboshuai.demo.readwritelock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 1: 演示读写锁的基本用法，模拟一个简单的缓存实现。
 * 目标：允许多个线程同时读取缓存，但在写入缓存时，所有其他读写操作都必须等待。
 */
public class BasicUsageCache {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Cache cache = new Cache();
        threadPool.execute(new WriteTask(cache));
        for (int i = 0; i < 8; i++) {
            threadPool.execute(new ReadTask(cache));
        }
        threadPool.execute(new WriteTask(cache));
        threadPool.shutdown();
    }

    static class Cache {

        private final Map<String, Object> cache = new HashMap<>();

        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        private final Lock readLock = lock.readLock();

        private final Lock writeLock = lock.writeLock();

        /**
         * 从缓存中获取数据
         */
        public Object get(String key) {
            readLock.lock();
            try {
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 5000 + 1000));
                Object value = cache.get(key);
                System.out.printf("[%s] 从缓存中获取数据，key为[%s]，获取到的value为[%s]%n", Thread.currentThread().getName(), key, value.toString());
                return value;
             } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * 向缓存中设置数据
         */
        public void set(String key, Object value) {
            writeLock.lock();
            try {
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 5000 + 1000));
                cache.put(key, value);
                System.out.printf("[%s] 向缓存中设置数据，key为[%s]，存进去的value为[%s]%n", Thread.currentThread().getName(), key, value.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                writeLock.unlock();
            }
        }
    }

    static class ReadTask implements Runnable {

        private final Cache cache;

        public ReadTask(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            Object ignore = cache.get("testKey");
        }
    }

    static class WriteTask implements Runnable {

        private final Cache cache;

        public WriteTask(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            cache.set("testKey", "testValue");
        }
    }
}