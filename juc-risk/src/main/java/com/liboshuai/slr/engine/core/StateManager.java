package com.liboshuai.slr.engine.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liboshuai.slr.engine.juc.SlidingWindowCounter;
import com.liboshuai.slr.engine.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 状态管理器
 * 职责：
 * 1. 维护内存中的热点 Key 计数器 (Caffeine/Map)
 * 2. 定时将内存状态异步刷入 Redis (Persistence)
 * 3. 系统启动时从 Redis 预热恢复 (Recovery)
 */
@Slf4j
@Component
public class StateManager {

    private final RedissonClient redissonClient;

    // 内存热点状态：Key -> 滑动窗口计数器
    // 使用 Caffeine 避免内存溢出，设置过期时间防止冷数据长期占用
    private final Cache<String, SlidingWindowCounter> stateCache;

    // 记录哪些 Key 是脏数据（需要刷盘），ConcurrentHashMap.KeySetView 也就是并发 Set
    private final Map<String, Boolean> dirtyKeys = new ConcurrentHashMap<>();

    private final ScheduledExecutorService persistenceExecutor;

    public StateManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.stateCache = Caffeine.newBuilder()
                .maximumSize(100_000) // 最多缓存 10w 个热点用户/IP
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

        this.persistenceExecutor = new ScheduledThreadPoolExecutor(2, r -> {
            Thread t = new Thread(r, "state-persistence-worker");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    public void startPersistenceTask() {
        // 每 5 秒执行一次刷盘操作
        persistenceExecutor.scheduleAtFixedRate(this::flushDirtyStates, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 更新状态（线程安全）
     */
    public void updateState(RiskEvent event) {
        // 假设我们需要统计: userId 在过去 60s 的访问次数
        String key = "user_freq_60s:" + event.getUserId();

        SlidingWindowCounter counter = stateCache.get(key, k -> {
            // Cache Miss: 从 Redis 恢复或新建
            return loadFromRedisOrNew(k, 60);
        });

        if (counter != null) {
            counter.add(event.getTimestamp(), 1);
            // 标记为脏数据
            dirtyKeys.put(key, true);
        }
    }

    /**
     * 获取当前计数值（供规则引擎使用）
     */
    public long getCount(String key) {
        SlidingWindowCounter counter = stateCache.getIfPresent(key);
        if (counter != null) {
            return counter.sum();
        }
        // 如果内存没有，尝试去 Redis 查（防止冷启动误判）
        // 简化逻辑：这里暂不实现复杂的 Redis 读取，默认返回 0
        return 0;
    }

    /**
     * 从 Redis 加载状态 (模拟 Checkpoint 恢复)
     */
    private SlidingWindowCounter loadFromRedisOrNew(String key, int windowSize) {
        // 实际生产中，Redis 可能存储的是 snapshot count
        // 这里简化为：如果没有，直接创建新的
        return new SlidingWindowCounter(windowSize);
    }

    /**
     * 异步刷盘逻辑
     */
    public void flushDirtyStates() {
        if (dirtyKeys.isEmpty()) return;

        log.info("Flushing {} dirty keys to Redis...", dirtyKeys.size());

        // 获取所有脏 Key 的快照
        Object[] keys = dirtyKeys.keySet().toArray();
        dirtyKeys.clear(); // 清空脏标记

        RMap<String, Long> redisMap = redissonClient.getMap("slr_risk_state");

        for (Object kObj : keys) {
            String key = (String) kObj;
            SlidingWindowCounter counter = stateCache.getIfPresent(key);
            if (counter != null) {
                // 将当前窗口总数写入 Redis (简化版，实际可能需要存 buckets 明细)
                // 使用 fastPutAsync 异步写入，提高吞吐
                redisMap.fastPutAsync(key, counter.sum());
            }
        }
    }

    /**
     * 全量刷盘（停机时调用）
     */
    public void flushAllStates() {
        log.info("Force flushing all states...");
        flushDirtyStates();
    }
}