package com.liboshuai.demo.juc.problem;

import java.util.concurrent.*;

/**
 * 第八关：CompletableFuture 异步编排
 * 场景：Flink Async I/O，并发查询外部数据并合并
 */
public class ConcurrencyTask08 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 模拟 Flink 的自定义业务线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        System.out.println("主线程：开始处理请求...");
        long start = System.currentTimeMillis();

        // --- 任务开始 ---
        int userId = 1;

        // TODO: 任务 A - 异步查询用户信息 (返回 String)
        // CompletableFuture<String> userFuture = ...
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> ConcurrencyTask08.fetchUser(userId), threadPool);
        // TODO: 任务 A - 异步查询信用分 (返回 Double)
        // CompletableFuture<Double> scoreFuture = ...
        CompletableFuture<Double> scoreFuture = CompletableFuture.supplyAsync(() -> fetchScore(userId), threadPool);

        // TODO: 任务 B - 并行合并结果 (thenCombine)
        // 两个都完成后，返回格式: "User: [name], Score: [score]"
        // CompletableFuture<String> resultFuture = ...
        CompletableFuture<String> resultFuture = userFuture.thenCombine(scoreFuture, (user, score) -> "User: [" + user + "], Score: [" + score + "]");

        // TODO: 任务 C - 异常兜底 (exceptionally)
        // 如果发生异常，返回 "Unknown User"
        resultFuture = resultFuture.exceptionally(throwable -> "Unknown User");

        // --- 任务结束 ---

        // 主线程获取结果 (为了演示效果，这里我们可以阻塞等待，但在 Flink 内部是回调机制)
         String result = resultFuture.get();
         System.out.println("最终结果: " + result);
         System.out.println("总耗时: " + (System.currentTimeMillis() - start) + "ms");

        threadPool.shutdown();
    }

    // 模拟 RPC 调用：查询用户
    private static String fetchUser(int id) {
        sleep(100); // 模拟网络延迟
        if (id == -1) throw new RuntimeException("查无此人");
        return "User-" + id;
    }

    // 模拟 RPC 调用：查询分数
    private static Double fetchScore(int id) {
        sleep(200); // 模拟网络延迟
        return 85.5;
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    /*
     * TODO: 思考题
     * thenApply 和 thenCompose 的区别是什么？
     * 答：thenApply和map相似, 而thenCompose与FlatMap相似. 如果衔接的任务是completableFuture, 那么必须使用thenCompose, 否则会出现嵌套CompletableFuture的情况.
     */
}
