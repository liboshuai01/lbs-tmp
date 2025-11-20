package com.liboshuai.demo.juc.problem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 第五关：读写分离 ReentrantReadWriteLock
 * 场景：Flink 动态配置管理 (读多写少)
 */
public class ConcurrencyTask05 {

    // 模拟读取线程数量（高并发读）
    private static final int READER_COUNT = 10;
    // 模拟写入线程数量（低并发写）
    private static final int WRITER_COUNT = 2;

    public static void main(String[] args) {
        ConfigManager configManager = new ConfigManager();

        // 启动写入线程
        for (int i = 0; i < WRITER_COUNT; i++) {
            new Thread(() -> {
                while (true) {
                    configManager.updateConfig("timeout", "2000");
                    sleep(500); // 写入频率较低
                }
            }, "Writer-" + i).start();
        }

        // 启动读取线程
        for (int i = 0; i < READER_COUNT; i++) {
            new Thread(() -> {
                while (true) {
                    configManager.readConfig("timeout");
                    sleep(50); // 读取频率极高
                }
            }, "Reader-" + i).start();
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }

    static class ConfigManager {
        private final Map<String, String> configMap = new HashMap<>();

        // TODO: 任务 A - 替换为 ReentrantReadWriteLock
        // private final ReadWriteLock rwLock = ...
        // 原始的排他锁（性能瓶颈）
//        private final Lock lock = new ReentrantLock();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Lock readLock = lock.readLock();
        private final Lock writeLock = lock.writeLock();

        public ConfigManager() {
            configMap.put("timeout", "1000");
        }

        // 高频读取：多个线程应该能同时读，不该互斥
        public String readConfig(String key) {
            // TODO: 任务 B - 使用 读锁
            readLock.lock(); // 现在的实现是排他的，效率极低
            try {
                // 模拟读取耗时
                sleep(10);
                String value = configMap.get(key);
                System.out.println(Thread.currentThread().getName() + " 读取: " + key + " = " + value);
                return value;
            } finally {
                readLock.unlock();
            }
        }

        // 低频写入：必须排他，写的时候不允许任何人读或写
        public void updateConfig(String key, String value) {
            // TODO: 任务 C - 使用 写锁
            writeLock.lock();
            try {
                System.out.println(">>> " + Thread.currentThread().getName() + " 开始更新...");
                // 模拟写入耗时
                sleep(200);
                configMap.put(key, value);
                System.out.println("<<< " + Thread.currentThread().getName() + " 更新完成: " + key + " = " + value);
            } finally {
                writeLock.unlock();
            }
        }

        private void sleep(long ms) {
            try { Thread.sleep(ms); } catch (InterruptedException e) { }
        }
    }

    /*
     * TODO: 思考题
     * 1. 写锁能降级为读锁吗？（持有写锁的过程中去获取读锁）
     * 答：可以, 这称为锁降级.
     * 2. 读锁能升级为写锁吗？（持有读锁的过程中去获取写锁）
     * 答：不可以, 不支持锁升级.
     */
}
