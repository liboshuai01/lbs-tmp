package com.liboshuai.demo.cf;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture.anyOf()
 * 场景：服务竞速（Racing）以实现高可用/低延迟。
 * 1. 假设我们在全球有两个相同的 "天气服务" 实例 (US 和 EU)。
 * 2. (Fan-out) 我们 *同时* 向两个实例发起异步请求。
 * 3. (Racing) 使用 .anyOf() 等待 *第一个* 成功返回的结果。
 * 4. (First Result) 立即将这个最快的结果返回给用户，并取消其他慢的请求。
 */
public class RacingServicesDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型
    // ----------------------------------------------------------------
    record Weather(String sourceRegion, double temperature) {}

    private static final Random RANDOM = new Random();

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”
    // ----------------------------------------------------------------

    /**
     * 模拟的API调用：获取天气
     * @param region 服务器所在的区域
     * @param forcedLatency 模拟的该区域网络延迟
     */
    private static CompletableFuture<Weather> fetchWeatherAsync(
            String region, int forcedLatency, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            log("开始连接 [" + region + "] 服务... (延迟: " + forcedLatency + "ms)");
            try {
                Thread.sleep(forcedLatency);
            } catch (InterruptedException e) { /* ... */ }

            // 模拟随机失败
            if (region.contains("FAIL")) {
                log("[" + region + "] 服务查询失败！");
                throw new RuntimeException("Service " + region + " is down");
            }

            log("[" + region + "] 服务成功返回。");
            return new Weather(region, 18.0 + RANDOM.nextDouble() * 5); // 18-23 度
        }, executor);
    }

    // ----------------------------------------------------------------
    // 3. 主流程 (Main)
    // ----------------------------------------------------------------
    public static void main(String[] args) {
        log("主线程开始运行...");

        ExecutorService ioExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        // ================================================================
        // 演示 1: US 服务更快
        // ================================================================
        log("\n--- 演示 1: US (1000ms) vs EU (1500ms) ---");
        runRace(
                fetchWeatherAsync("us-east", 1000, ioExecutor),
                fetchWeatherAsync("eu-west", 1500, ioExecutor)
        );

        // ================================================================
        // 演示 2: EU 服务更快 (且 US 服务最终会失败)
        // ================================================================
        log("\n--- 演示 2: US (1500ms, FAIL) vs EU (1000ms) ---");
        runRace(
                fetchWeatherAsync("us-east-FAIL", 1500, ioExecutor),
                fetchWeatherAsync("eu-west", 1000, ioExecutor)
        );

        // ================================================================
        // 演示 3: 两个服务都失败
        // ================================================================
        log("\n--- 演示 3: US (1000ms, FAIL) vs EU (800ms, FAIL) ---");
        runRace(
                fetchWeatherAsync("us-east-FAIL", 1000, ioExecutor),
                fetchWeatherAsync("eu-west-FAIL", 800, ioExecutor)
        );

        ioExecutor.shutdown();
        log("\n主线程结束。");
    }

    /**
     * 运行一次竞速的辅助方法
     */
    private static void runRace(CompletableFuture<Weather> usFuture,
                                CompletableFuture<Weather> euFuture) {

        List<CompletableFuture<Weather>> futures = List.of(usFuture, euFuture);

        // ================================================================
        // 核心演示：CompletableFuture.anyOf()
        //
        // 1. CompletableFuture.anyOf() 接收一个 Future 数组
        // 2. 它返回一个 *新* 的 CompletableFuture<Object>
        // 3. 这个新 Future 会在 *任何一个* 原始 Future *完成* 时立即完成。
        //    (注意："完成" 包括 *成功* 或 *失败*)
        // ================================================================
        CompletableFuture<Object> firstToFinishFuture = CompletableFuture.anyOf(
                usFuture, euFuture
        );

        // 步骤 3: 处理第一个返回的结果
        CompletableFuture<Void> finalChain = firstToFinishFuture
                .thenAccept(firstResult -> {
                    // *** 成功路径 ***
                    // 只要 *第一个* 完成的 Future 是 *成功* 的，这个块就会执行。
                    //
                    // 注意：firstResult 是一个 Object，你需要自己转换类型。
                    Weather weather = (Weather) firstResult;
                    log("--- 竞速 [成功]! 最快的结果来自: [" + weather.sourceRegion() + "] ---");
                    log("  -> 天气: " + String.format("%.1f", weather.temperature()) + "°C");

                    // (最佳实践) 取消其他仍在运行的慢任务
                    cancelSlowFutures(futures, firstToFinishFuture);
                })
                .exceptionally(ex -> {
                    // *** 异常路径 ***
                    // 如果 *第一个* 完成的 Future 是 *失败* 的，
                    // .anyOf() 会立即以该异常失败，
                    // .thenAccept() 会被 *跳过*，
                    // .exceptionally() 会被执行。
                    log("--- 竞速 [失败]! 第一个完成的任务抛出了异常 ---");
                    log("  -> 错误: " + ex.getCause().getMessage());
                    // 注意：如果所有任务都失败了，这里会捕获到 *最先* 失败的那个。
                    return null; // 返回 Void
                });

        try {
            // 阻塞，直到 .thenAccept 或 .exceptionally 执行完毕
            finalChain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log("主线程等待时出错: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：取消那些比 "获胜者" 慢的 Future
     */
    private static void cancelSlowFutures(
            List<CompletableFuture<Weather>> futures,
            CompletableFuture<Object> winnerFuture) {

        log("...正在取消其他慢速任务...");
        for (CompletableFuture<Weather> future : futures) {
            // Java 8 没有 .isDone()，但我们可以用 winnerFuture
            // (Java 9+ 可以用 future.resultNow() 或 winnerFuture.resultNow())
            // 这里的逻辑是：如果这个 future 不是获胜者，就取消它

            // 简单起见，我们直接尝试取消所有 *未完成* 的
            // (包括获胜者，但取消一个已完成的 Future 是无害的)
            if (!future.isDone()) {
                future.cancel(true); // true = 尝试中断线程
                if (future.isCancelled()) {
                    log("  -> 成功取消了一个慢速任务。");
                }
            }
        }
    }

    /** 辅助方法：打印日志并带上线程名 */
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
