package com.liboshuai.demo.juc.problem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 第一关：原子性与可见性实战
 * 模拟 Flink 算子处理数据并统计 Metric 的场景
 */
public class ConcurrencyTask01 {

    // 模拟总的处理次数：10个线程 * 每个线程10000次 = 100,000次
    private static final int THREAD_COUNT = 10;
    private static final int LOOP_COUNT = 10000;

    public static void main(String[] args) throws InterruptedException {
        // --- 任务 A: 原始的不安全计数器 ---
        System.out.println("--- 开始测试: 不安全的计数器 ---");
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        runTest(unsafeCounter);

        // --- 任务 B: 请在此处实例化你修复后的 SyncCounter ---
         System.out.println("\n--- 开始测试: Synchronized 修复后的计数器 ---");
         Counter syncCounter = new SyncCounter();
         runTest(syncCounter);

        // --- 任务 C: 请在此处实例化你修复后的 AtomicCounter ---
         System.out.println("\n--- 开始测试: Atomic 修复后的计数器 ---");
         Counter atomicCounter = new AtomicCounter();
         runTest(atomicCounter);
    }

    /**
     * 测试脚手架：启动多线程模拟高并发调用
     */
    private static void runTest(Counter counter) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        // 用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            threadPool.execute(() -> {
                try {
                    for (int j = 0; j < LOOP_COUNT; j++) {
                        counter.inc();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程结束
        latch.await();
        long end = System.currentTimeMillis();
        threadPool.shutdown();

        long actual = counter.get();
        long expected = (long) THREAD_COUNT * LOOP_COUNT;

        System.out.println("期望值: " + expected);
        System.out.println("实际值: " + actual);
        System.out.println("耗时: " + (end - start) + "ms");

        if (actual != expected) {
            System.err.println("结果错误！存在线程安全问题。");
        } else {
            System.out.println("结果正确！");
        }
    }

    // --- 接口定义 ---
    interface Counter {
        void inc();
        long get();
    }

    // -------------------------------------------------------
    // 任务 A: 分析这个类为什么不安全，即使加了 volatile
    // -------------------------------------------------------
    /*
    TODO 回答:
        因为volatile只保证了可见性, 不能保证原子性. count++不是原子性操作, 会被分为三步: 获取变量原值 -> 原值+1 -> 将新值写入变量.
        如果有两个线程同时执行步骤一, 本来最后因为+2,但是最后只会+1.
     */
    static class UnsafeCounter implements Counter {
        // volatile 保证了可见性，但能保证原子性吗？
        private volatile long count = 0;

        @Override
        public void inc() {
            // 这里的操作是：读 -> 改 -> 写
            count++;
        }

        @Override
        public long get() {
            return count;
        }
    }

    // -------------------------------------------------------
    // 任务 B: 请实现基于 synchronized 的解决方案
    // -------------------------------------------------------
    static class SyncCounter implements Counter {
        private volatile long count = 0; // TODO: 新的疑问, 这里使用volatile, 那么 get() 是否可以不加锁呢? 如果不加锁, 是否存在问题?

        @Override
        public void inc() {
            // TODO: 在此补充代码
            synchronized (this){
                count++;
            }
        }

        @Override
        public long get() {
            // TODO: 在此补充代码 (思考：读操作需要加锁吗？)
            // TODO 回答: 读操作同样需要加锁, 如果不加锁这里面没办法从主内存中获取到最新的变量值
//            synchronized (this) {
//                return count;
//            }
            return count; // TODO: 新的疑问, 这里使用volatile, 那么 get() 是否可以不加锁呢? 如果不加锁, 是否存在问题?
        }
    }

    // -------------------------------------------------------
    // 任务 C: 请实现基于 Atomic 的解决方案 (模拟 Flink 源码中的做法)
    // -------------------------------------------------------
    static class AtomicCounter implements Counter {
        // TODO: 定义 AtomicLong
        private final AtomicLong count = new AtomicLong(0);

        @Override
        public void inc() {
            // TODO: 使用 AtomicLong 的方法
            count.incrementAndGet();
        }

        @Override
        public long get() {
            // TODO: 获取值
            return count.get();
        }
    }
}
