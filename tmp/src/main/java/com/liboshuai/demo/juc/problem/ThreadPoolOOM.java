package com.liboshuai.demo.juc.problem;


import java.util.concurrent.*;

public class ThreadPoolOOM {

    // 🛑 看起来很合理的 "固定大小线程池"
    // 只有 10 个线程干活
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        // 模拟突发流量：瞬间涌入 100 万个任务
        for (int i = 0; i < 1_000_000; i++) {
            pool.submit(() -> {
                try {
                    // 模拟写入数据库耗时 1 秒
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) { }
            });
        }
        System.out.println("任务提交完毕");
    }
}