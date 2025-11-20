package com.liboshuai.demo.juc.problem;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncOrchestration {

    public static void main(String[] args) {
        // 任务 1: 获取基本信息 (正常)
        CompletableFuture<String> infoFuture = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "iPhone 15";
        });

        // 任务 2: 获取价格 (模拟抛出异常)
        CompletableFuture<String> priceFuture = CompletableFuture.supplyAsync(() -> {
            sleep(300);
            // 🛑 模拟网络异常，服务挂了
            if (true) throw new RuntimeException("价格服务不可用！");
            return "9999元";
        });

        // 任务 3: 获取库存 (正常)
        CompletableFuture<String> stockFuture = CompletableFuture.supplyAsync(() -> {
            sleep(800);
            return "有货";
        });

        // 🛑 编排：等待所有任务完成 (AllOf)
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(infoFuture, priceFuture, stockFuture);

        System.out.println("开始等待结果...");

        // join() 会阻塞主线程，直到所有任务完成
        // 但是！如果其中一个任务异常了，join() 会发生什么？
        try {
            allFutures.join();
        } catch (Exception e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }

        // 🛑 即使这里捕获了，你知道是哪个 Future 错了吗？
        // 你能拿到 infoFuture 的结果吗？
        System.out.println("程序结束");
    }

    private static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { }
    }
}
