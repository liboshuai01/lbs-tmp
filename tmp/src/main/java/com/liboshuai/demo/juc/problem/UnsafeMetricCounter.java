package com.liboshuai.demo.juc.problem;


import java.util.concurrent.CountDownLatch;

/**
 * 这是一个简化的 Metrics 计数器模拟
 * 考察点：原子性 (Atomicity) 与 volatile 的局限性
 */
public class UnsafeMetricCounter {

    // -------------------------------------------------------
    // TODO: 这是一个用于统计处理记录数的变量，加了 volatile 看起来似乎很安全？
    // -------------------------------------------------------
    private volatile long recordCount = 0;

    /**
     * 模拟处理一条数据后，指标+1
     */
    public void inc() {
        recordCount++;
    }

    public long getCount() {
        return recordCount;
    }

    // -------------------------------------------------------
    // 测试入口
    // -------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        final int THREAD_COUNT = 10;
        final int INC_PER_THREAD = 10000;

        UnsafeMetricCounter counter = new UnsafeMetricCounter();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // 模拟 10 个线程，每个线程都并发地让计数器 +10000
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < INC_PER_THREAD; j++) {
                    counter.inc();
                }
                latch.countDown();
            }).start();
        }

        // 等待所有线程完成
        latch.await();

        long expected = THREAD_COUNT * INC_PER_THREAD;
        long actual = counter.getCount();

        System.out.println("Expected Count: " + expected);
        System.out.println("Actual Count:   " + actual);

        if (expected != actual) {
            System.err.println("测试失败：发生了数据丢失！volatile 并没有保证原子性。");
            System.exit(1);
        } else {
            System.out.println("测试通过？（如果通过了可能是运气好，或者是单核CPU）");
        }
    }
}