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
        r.lock();
        try {
            String value = cache.get(key);
            if (value != null) {
                return value;
            }
        } finally {
            r.unlock();
        }
        w.lock();
        try {
            String value = cache.get(key);
            if (value != null) {
                return value;
            }
            value = "db_value_" + key; // 模拟查库
            cache.put(key, value);
            return value;
        } finally {
            w.unlock();
        }
    }
}
