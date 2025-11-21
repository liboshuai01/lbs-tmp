package cn.liboshuai.demo.core.window;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

/**
 * 高性能滑动窗口计数器
 * * JUC 深度应用：使用 JDK 1.8 引入的 {@link StampedLock}。
 * 相比 ReentrantReadWriteLock，StampedLock 支持“乐观读”，
 * 在读多写少的场景下，几乎不发生锁竞争，也没有 CAS 开销，性能极高。
 */
public class SlidingWindowCounter implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private final int windowSizeInSeconds;
    private final int bucketCount;

    // 存储桶数据的数组
    @Getter
    @Setter
    private long[] buckets;

    // 记录每个桶归属的起始时间戳 (用于判断是否过期)
    @Getter
    @Setter
    private long[] bucketStartTimes;

    // StampedLock 不可重入，但性能最强
    // transient 不参与序列化，恢复时重新 new
    @JSONField(serialize = false, deserialize = false)
    private transient final StampedLock lock = new StampedLock();

    public SlidingWindowCounter(int windowSizeInSeconds) {
        this.windowSizeInSeconds = windowSizeInSeconds;
        // 精度目前定为 1秒1个桶
        this.bucketCount = windowSizeInSeconds;
        this.buckets = new long[bucketCount];
        this.bucketStartTimes = new long[bucketCount];

        // 初始化时间，避免刚启动时数据混乱
        long nowSec = System.currentTimeMillis() / 1000;
        Arrays.fill(bucketStartTimes, nowSec - windowSizeInSeconds);
    }

    /**
     * 增加计数 (写操作)
     */
    public void add(long count) {
        long nowSec = System.currentTimeMillis() / 1000;
        int idx = (int) (nowSec % bucketCount);

        // 使用写锁 (互斥)
        long stamp = lock.writeLock();
        try {
            long oldStartTime = bucketStartTimes[idx];
            if (nowSec != oldStartTime) {
                // 如果当前时间对应的桶不是当前秒的(说明是旧循环的数据)，重置
                buckets[idx] = 0;
                bucketStartTimes[idx] = nowSec;
            }
            buckets[idx] += count;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取当前窗口总数 (读操作 - 乐观锁优化)
     */
    public long sum() {
        long nowSec = System.currentTimeMillis() / 1000;
        long windowStart = nowSec - windowSizeInSeconds + 1;

        // 1. 尝试乐观读 (不加锁，只返回一个版本号 stamp)
        long stamp = lock.tryOptimisticRead();

        // 2. 在乐观模式下执行计算逻辑 (拷贝数据到栈上计算，防止读取过程中数组被修改)
        long total = calculateSum(nowSec, windowStart);

        // 3. 验证版本号。如果在计算期间锁被写线程获取过，validate 会返回 false
        if (!lock.validate(stamp)) {
            // 4. 乐观读失败，升级为悲观读锁
            stamp = lock.readLock();
            try {
                total = calculateSum(nowSec, windowStart);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return total;
    }

    // 内部计算逻辑，提取出来供乐观读和悲观读复用
    private long calculateSum(long nowSec, long windowStart) {
        long sum = 0;
        for (int i = 0; i < bucketCount; i++) {
            // 只有当桶的时间戳在当前窗口范围内才累加
            if (bucketStartTimes[i] >= windowStart && bucketStartTimes[i] <= nowSec) {
                sum += buckets[i];
            }
        }
        return sum;
    }
}