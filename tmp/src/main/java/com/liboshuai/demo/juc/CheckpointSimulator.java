package com.liboshuai.demo.juc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CheckpointSimulator {

    private static final int PARALLELISM = 3;

    // TODO: 定义 CyclicBarrier
    // 思考：构造函数里那个 Runnable 参数有什么用？在 Flink 里它对应什么？
    private final CyclicBarrier barrier;

    public CheckpointSimulator() {
        // TODO: 初始化 Barrier
        // 当 3 个线程都 await() 后，先执行这个 lambda，然后大家再一起苏醒
        barrier = new CyclicBarrier(PARALLELISM, () -> {
            System.out.println(">> All subtasks reached barrier. Triggering Checkpoint State Snapshot...");
        });
    }

    class SubTask implements Runnable {
        private final int id;

        public SubTask(int id) { this.id = id; }

        @Override
        public void run() {
            try {
                // --- 第一轮处理 ---
                System.out.println("SubTask-" + id + " is processing data phase 1...");
                Thread.sleep((long) (Math.random() * 1000));
                System.out.println("SubTask-" + id + " reached Barrier-1");

                // TODO: 等待其他人到达 Barrier-1
                // barrier.???
                barrier.await();

                // --- 第二轮处理 (模拟 Barrier 可重用) ---
                System.out.println("SubTask-" + id + " is processing data phase 2...");
                Thread.sleep((long) (Math.random() * 1000));
                System.out.println("SubTask-" + id + " reached Barrier-2");

                // TODO: 等待其他人到达 Barrier-2
                // barrier.???
                barrier.await();

                System.out.println("SubTask-" + id + " finished.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        for (int i = 0; i < PARALLELISM; i++) {
            new Thread(new SubTask(i)).start();
        }
    }

    public static void main(String[] args) {
        new CheckpointSimulator().start();
    }
}