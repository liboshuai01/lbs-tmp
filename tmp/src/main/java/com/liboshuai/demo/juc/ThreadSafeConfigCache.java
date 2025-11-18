package com.liboshuai.demo.juc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeConfigCache {

    private final Map<String, String> configMap = new HashMap<>();

    // TODO: 定义读写锁
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    // 高频读取：允许并发
    public String get(String key) {
        // TODO: 上锁
        rLock.lock();
        try {
            return configMap.get(key);
        } finally {
            // TODO: 解锁
            rLock.unlock();
        }
    }

    // 低频写入：独占
    public void put(String key, String value) {
        // TODO: 上锁
        wLock.lock();
        try {
            configMap.put(key, value);
            // 模拟耗时的写操作（比如写日志或持久化），更能体现读写锁优势
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } finally {
            // TODO: 解锁
            wLock.unlock();
        }
    }

    // 思考题：如果我在 put 方法里，想先读一下 key 是否存在，不存在再 put，
    // 能不能直接在持有 wLock 的时候调用 this.get(key)？
    /*
    回答:
        可以的! 这叫做锁降级. Java对于读写锁允许锁降级, 即写锁可以降级为读锁. 但是不允许锁升级, 即不允许读锁升级为写锁.
        对于我们修改后想要立即获取修改后的内容, 防止其他线程再次对其修改. 就可以使用锁降级, 大致步骤如下:
        获取写锁 -> 写入内容 -> 获取读锁 -> 释放写锁 -> 读取内容 -> 释放读锁.
     */
}
