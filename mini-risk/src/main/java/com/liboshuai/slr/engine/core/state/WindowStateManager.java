package com.liboshuai.slr.engine.core.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * JUC 核心：手动实现的时间滑动窗口状态机
 * 替代 Flink 的 Window 和 State Backend
 */
@Slf4j
@Component
public class WindowStateManager {

    // 外层 Map: Key -> RuleId (每个规则有独立的状态空间)
    // 内层 Map: Key -> TargetValue (如 userId) -> WindowState
    private final Map<Long, ConcurrentHashMap<String, WindowState>> stateStore = new ConcurrentHashMap<>();

    @Resource
    @Qualifier("windowCleanerExecutor")
    private ScheduledExecutorService cleanerExecutor;

    @PostConstruct
    public void init() {
        // 每秒执行一次清理任务，模拟 Watermark 推进，移除过期数据
        cleanerExecutor.scheduleAtFixedRate(this::cleanupExpiredState, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 获取或创建状态，并添加新事件
     */
    public void addEvent(Long ruleId, String targetKey, long eventTime, double value, int windowSizeSeconds) {
        stateStore.computeIfAbsent(ruleId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(targetKey, k -> new WindowState())
                .add(eventTime, value, windowSizeSeconds);
    }

    /**
     * 获取当前窗口聚合值
     */
    public double getAggregatedValue(Long ruleId, String targetKey, String aggType, int windowSizeSeconds) {
        ConcurrentHashMap<String, WindowState> ruleState = stateStore.get(ruleId);
        if (ruleState == null) return 0.0;

        WindowState windowState = ruleState.get(targetKey);
        if (windowState == null) return 0.0;

        // 获取当前时间，计算窗口聚合
        long currentTime = System.currentTimeMillis();
        return windowState.aggregate(aggType, currentTime, windowSizeSeconds);
    }

    /**
     * 清理过期数据的任务
     * 遍历所有状态，移除超出最大窗口时间的数据
     */
    private void cleanupExpiredState() {
        long currentTime = System.currentTimeMillis();
        // 简单的遍历清理策略。生产环境可能需要更精细的分片时间轮算法。
        stateStore.forEach((ruleId, keyMap) -> {
            Iterator<Map.Entry<String, WindowState>> iterator = keyMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WindowState> entry = iterator.next();
                WindowState state = entry.getValue();

                // 清理内部数据
                state.prune(currentTime, 3600); // 假设最大窗口不超过1小时

                // 如果状态为空，移除 Key，释放内存
                if (state.isEmpty()) {
                    iterator.remove();
                }
            }
        });
    }

    /**
     * 内部类：单个 Key 的窗口状态
     * JUC 亮点：使用 StampedLock 提供高性能的读写控制
     */
    public static class WindowState {
        // 存储事件的时间戳和数值
        // 也可以优化为 RingBuffer 或 Bucket Array 减少对象创建
        private final LinkedList<EventNode> events = new LinkedList<>();

        // StampedLock 比 ReentrantReadWriteLock 性能更好，特别是读多写少场景
        private final StampedLock lock = new StampedLock();

        public void add(long timestamp, double value, int windowSizeSeconds) {
            long stamp = lock.writeLock();
            try {
                events.addLast(new EventNode(timestamp, value));
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        public double aggregate(String type, long currentTime, int windowSizeSeconds) {
            long startTime = currentTime - (windowSizeSeconds * 1000L);
            double result = 0.0;

            // 乐观读锁尝试
            long stamp = lock.tryOptimisticRead();
            // 这里简化逻辑：直接使用悲观读锁，因为这就涉及到遍历链表
            stamp = lock.readLock();
            try {
                for (EventNode node : events) {
                    if (node.timestamp >= startTime) {
                        if ("COUNT".equalsIgnoreCase(type)) {
                            result += 1;
                        } else if ("SUM".equalsIgnoreCase(type)) {
                            result += node.value;
                        }
                    }
                }
            } finally {
                lock.unlockRead(stamp);
            }
            return result;
        }

        public void prune(long currentTime, int maxKeepSeconds) {
            long threshold = currentTime - (maxKeepSeconds * 1000L);
            long stamp = lock.writeLock();
            try {
                // 移除过期数据
                while (!events.isEmpty() && events.getFirst().timestamp < threshold) {
                    events.removeFirst();
                }
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        public boolean isEmpty() {
            long stamp = lock.tryOptimisticRead();
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    return events.isEmpty();
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return events.isEmpty();
        }

        @Data
        @AllArgsConstructor
        private static class EventNode {
            long timestamp;
            double value;
        }
    }
}