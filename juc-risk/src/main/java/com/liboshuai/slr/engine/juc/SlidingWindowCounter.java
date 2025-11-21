package com.liboshuai.slr.engine.juc;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * 基于 JUC 实现的高性能滑动窗口计数器 (替代 Redis ZSet / Flink Window)
 * 核心思想：环形数组 (RingBuffer) + 分桶 (Bucket) + 惰性重置
 *
 * 结构：
 * [Bucket 0] [Bucket 1] ... [Bucket N-1]
 * 每个 Bucket 存储该秒内的计数值。
 */
public class SlidingWindowCounter {

    // 窗口大小（秒），例如 60秒
    private final int windowSizeInSeconds;
    // 环形数组，存储每一秒的统计数据
    private final AtomicReferenceArray<Bucket> ringBuffer;

    public SlidingWindowCounter(int windowSizeInSeconds) {
        this.windowSizeInSeconds = windowSizeInSeconds;
        // 初始化环形数组
        this.ringBuffer = new AtomicReferenceArray<>(windowSizeInSeconds);
    }

    /**
     * 增加计数
     * @param timestampEvent 事件发生的时间戳 (毫秒)
     * @param count 增加的数量
     */
    public void add(long timestampEvent, int count) {
        long timeInSeconds = timestampEvent / 1000;
        int index = (int) (timeInSeconds % windowSizeInSeconds);

        while (true) { // CAS 自旋重试
            Bucket oldBucket = ringBuffer.get(index);

            if (oldBucket == null) {
                // 初始化桶，使用 LongAdder 保证高并发写入性能
                Bucket newBucket = new Bucket(timeInSeconds);
                newBucket.adder.add(count);
                if (ringBuffer.compareAndSet(index, null, newBucket)) {
                    return;
                }
            } else if (oldBucket.windowStartSecond == timeInSeconds) {
                // 命中当前时间窗口，直接累加
                oldBucket.adder.add(count);
                return;
            } else if (oldBucket.windowStartSecond < timeInSeconds) {
                // 这是一个旧数据，需要重置该桶（轮次滚动）
                // 比如 index=5 原本存的是 10:00:05 的数据，现在来了 10:01:05 的数据
                Bucket newBucket = new Bucket(timeInSeconds);
                newBucket.adder.add(count);
                if (ringBuffer.compareAndSet(index, oldBucket, newBucket)) {
                    return;
                }
            } else {
                // 异常情况：收到了比当前桶还旧的数据（乱序严重），或者时间回拨
                // 对于风控场景，过期的迟到数据通常可以直接丢弃，或者记录到侧输出流
                return;
            }
        }
    }

    /**
     * 获取当前滑动窗口内的总数
     * @return 总计数
     */
    public long sum() {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long sum = 0;

        for (int i = 0; i < windowSizeInSeconds; i++) {
            Bucket bucket = ringBuffer.get(i);
            if (bucket != null) {
                // 只有在窗口范围内的时间桶才计入
                // 例如：当前 10:05:00，窗口60s。如果桶里是 10:03:xx 的数据，则不计入
                if (currentTimeSeconds - bucket.windowStartSecond < windowSizeInSeconds) {
                    sum += bucket.adder.sum();
                }
            }
        }
        return sum;
    }

    /**
     * 内部桶结构
     */
    private static class Bucket {
        final long windowStartSecond;
        final LongAdder adder; // JUC 神器：比 AtomicLong 在高并发下性能高数倍

        Bucket(long windowStartSecond) {
            this.windowStartSecond = windowStartSecond;
            this.adder = new LongAdder();
        }
    }

    /**
     * 用于状态恢复：强制设置某个时间点的值
     */
    public void recover(long windowStartSecond, long count) {
        int index = (int) (windowStartSecond % windowSizeInSeconds);
        Bucket bucket = new Bucket(windowStartSecond);
        bucket.adder.add(count);
        ringBuffer.set(index, bucket);
    }
}