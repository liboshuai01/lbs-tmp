package com.liboshuai.demo.juc.problem;

import java.util.concurrent.CountDownLatch;

/**
 * 第七关：CountDownLatch
 * 场景：模拟 Flink 压力测试，同时开始，等待结束
 */
public class ConcurrencyTask07 {

    private static final int THREAD_COUNT = 5;

    public static void main(String[] args) throws InterruptedException {
        // TODO: 任务 A - 定义两个 CountDownLatch
        // 1. startGate: 控制所有线程同时开始 (计数为 1)
        // 2. endGate: 等待所有线程结束 (计数为 THREAD_COUNT)
        CountDownLatch startGate = new CountDownLatch(1);
        // CountDownLatch endGate = ...
        CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(new Worker(i, startGate, endGate)).start(); // 这里 null 需要替换为 endGate
        }

        System.out.println("主线程：正在准备...");
        Thread.sleep(1000); // 模拟准备工作

        // TODO: 任务 C - 主线程操作
        System.out.println("主线程：发令！开始压测！");
        // 1. 打开开始门 (startGate)
        startGate.countDown();
        // 2. 等待结束门 (endGate)
        endGate.await();

        System.out.println("主线程：所有任务完成，开始统计数据。");
    }

    static class Worker implements Runnable {
        private final int id;
        private final CountDownLatch startGate;
        private final CountDownLatch endGate;

        public Worker(int id, CountDownLatch startGate, CountDownLatch endGate) {
            this.id = id;
            this.startGate = startGate;
            this.endGate = endGate;
        }

        @Override
        public void run() {
            try {
                System.out.println("Worker-" + id + " 已就绪，等待发令...");
                // TODO: 任务 B - 子线程操作
                // 1. 阻塞等待发令 (await startGate)
                startGate.await();
                // 模拟业务逻辑
                doWork();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 2. 完成任务，打卡 (countDown endGate)
                endGate.countDown();
            }
        }

        private void doWork() {
            System.out.println("Worker-" + id + " 正在执行任务...");
        }
    }

    /*
     * TODO: 思考题
     * CountDownLatch 和 CyclicBarrier 的核心区别是什么？
     * 答：CountDownLath 通常用于一等多或者多等一的场景, CyclicBarrier 通常用于多等多的场景. 且 CyclicBarrier 可以重复使用, 而 CountDownLatch 不行.
     */
}
