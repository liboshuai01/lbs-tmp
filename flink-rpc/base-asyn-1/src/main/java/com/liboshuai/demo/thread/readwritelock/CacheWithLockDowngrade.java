package com.liboshuai.demo.thread.readwritelock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例：使用锁降级实现一个高性能的线程安全缓存
 *
 * 业务场景：
 * 我们实现一个缓存，用于存储需要昂贵计算才能获得的数据。
 * 1. 当缓存命中且数据有效时，所有线程可以直接并发读取。
 * 2. 当缓存未命中或数据失效时：
 * - 需要有一个线程负责重新计算或从数据源获取数据，并放入缓存。
 * - 在该线程计算并更新缓存期间，不应有其他线程进行写操作。
 * - 一旦数据更新完成，该线程需要立即使用该数据，同时其他等待的读线程也应该能立即读到新数据。
 *
 * 锁降级在此场景下的作用：
 * 一个线程获取写锁更新了缓存后，它需要保证自己能原子性地读取到刚更新的数据，
 * 同时为了提高并发性，它希望在读取期间允许其他线程也来读取（但不允许写）。
 * 这通过“持有写锁 -> 获取读锁 -> 释放写锁”的降级过程来实现。
 */
public class CacheWithLockDowngrade<K, V> {

    // 使用 Map 模拟缓存存储
    private final Map<K, V> cacheMap = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    /**
     * 从缓存中获取数据。如果缓存不存在，则通过 dataProducer 计算并放入缓存。
     *
     * @param key          缓存键
     * @param dataProducer 如果缓存不存在，用于生成数据的回调函数
     * @return 缓存中的数据
     */
    public V get(K key, DataProducer<K, V> dataProducer) {
        V value;

        // 步骤 1: 尝试获取读锁，检查缓存是否命中
        readLock.lock();
        System.out.println(Thread.currentThread().getName() + " 获取了读锁，尝试读取缓存...");
        try {
            value = cacheMap.get(key);
            if (value != null) {
                System.out.println(Thread.currentThread().getName() + " 缓存命中，值为: " + value);
                return value; // 缓存命中，直接返回
            }
        } finally {
            System.out.println(Thread.currentThread().getName() + " 释放了读锁（初次尝试）。");
            readLock.unlock();
        }

        // 步骤 2: 缓存未命中，必须获取写锁来生成和写入数据
        System.out.println(Thread.currentThread().getName() + " 缓存未命中，准备获取写锁...");
        writeLock.lock();
        System.out.println(Thread.currentThread().getName() + " 获取了写锁。");
        try {
            // 双重检查：可能在释放读锁和获取写锁的间隙，已有其他线程更新了缓存
            value = cacheMap.get(key);
            if (value != null) {
                System.out.println(Thread.currentThread().getName() + " 在获取写锁后，发现缓存已被其他线程填充。");
                // 在这里直接返回 value 也可以，但为了演示完整的锁降级流程，我们继续执行。
            } else {
                // 缓存确实不存在，执行昂贵的数据生成操作
                System.out.println(Thread.currentThread().getName() + " 正在执行昂贵的数据计算/获取操作...");
                value = dataProducer.produce(key);
                cacheMap.put(key, value);
                System.out.println(Thread.currentThread().getName() + " 数据计算完成并已写入缓存。");
            }


            // 步骤 3: 锁降级！在释放写锁之前，获取读锁。
            // 目的：保证当前线程能以原子方式读取刚写入的数据，并在此期间允许其他线程并发读取，但阻止写入。
            readLock.lock();
            System.out.println(Thread.currentThread().getName() + "【锁降级】在持有写锁的情况下，获取了读锁。");

        } finally {
            // 步骤 4: 释放写锁。当前线程仍然持有读锁。
            writeLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放了写锁，但仍持有读锁。");
        }

        // 步骤 5: 在只持有读锁的情况下，对新数据进行后续处理
        // 此刻，其他读线程可以并发进入，但所有写线程都会被阻塞，保证了我们读取的数据不会被修改。
        try {
            System.out.println(Thread.currentThread().getName() + " 持有读锁，安全地使用刚获取的数据: " + value);
            // ... 可以在这里执行一些基于新数据的只读操作 ...
            return value;
        } finally {
            readLock.unlock();
            System.out.println(Thread.currentThread().getName() + " 释放了读锁，操作完成。");
        }
    }

    // 用于模拟数据生产的函数式接口
    @FunctionalInterface
    interface DataProducer<K, V> {
        V produce(K key);
    }

    public static void main(String[] args) {
        CacheWithLockDowngrade<String, String> cache = new CacheWithLockDowngrade<>();

        // 定义一个昂贵的数据生产过程
        DataProducer<String, String> producer = key -> {
            try {
                // 模拟耗时操作，如数据库查询或复杂计算
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Computed-Value-For-" + key;
        };

        // 启动多个线程并发访问缓存
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            new Thread(() -> {
                System.out.println("线程 " + threadNum + " 开始执行...");
                String value = cache.get("data-key", producer);
                System.out.println("线程 " + threadNum + " 获取到的最终值为: " + value);
            }, "Reader-Writer-" + i).start();
        }
    }
}