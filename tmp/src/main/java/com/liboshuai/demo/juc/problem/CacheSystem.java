package com.liboshuai.demo.juc.problem;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheSystem {

    private final Map<String, String> cache = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock r = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();

    public String get(String key) {
        // 1. 先加读锁，允许多个线程并发读取
        r.lock();
        try {
            String value = cache.get(key);
            if (value != null) {
                return value; // 缓存命中，直接返回
            }
        } finally {
            // 🛑 既然没命中，我需要写缓存。
            // 但为了保持原子性（防止我释放读锁后被别人抢先写了），
            // 我决定 **不释放读锁**，直接去申请写锁！
            // (这就是所谓的 "锁升级" 意图)
            System.out.println(Thread.currentThread().getName() + " 尝试获取写锁...");
            w.lock(); // <--- ⚠️ 致命代码在这里
            try {
                //再次检查(双重检查)
                String value = cache.get(key);
                if(value == null){
                    value = "db_value_" + key; // 模拟查库
                    cache.put(key, value);
                }
                return value;
            } finally {
                w.unlock();
                r.unlock(); // 最后释放读锁
            }
        }
    }
}
