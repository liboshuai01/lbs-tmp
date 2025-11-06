package com.liboshuai.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture.exceptionally()
 * 场景：服务容错。
 * 1. 尝试从 "主服务" 异步获取配置。
 * 2. "主服务" 调用失败 (我们在此模拟一个异常)。
 * 3. 使用 .exceptionally() 捕获这个异常。
 * 4. 在 exceptionally 块中，返回一个 *同步* 的 "默认兜底配置"。
 * 5. 链条被 "治愈"，后续的 .thenAccept 会收到这个默认配置并正常执行。
 */
public class FaultToleranceDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型
    // ----------------------------------------------------------------
    record Config(String databaseUrl, int connectionPoolSize) {}

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”
    // ----------------------------------------------------------------

    /**
     * 模拟的API调用：异步获取主配置
     * (我们将 *故意* 让这个方法失败)
     */
    private static CompletableFuture<Config> fetchConfigFromPrimary(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            log("开始尝试连接 [主] 配置服务...");
            try {
                Thread.sleep(500); // 模拟短暂的连接时间
            } catch (InterruptedException e) { /* ... */ }

            // 模拟服务500错误或网络超时
            log("[主] 服务连接失败！抛出异常。");
            throw new RuntimeException("主配置服务不可达 (500 Error)");

            // (这行代码永远不会执行)
            // return new Config("jdbc:mysql://primary/prod_db", 50);

        }, executor);
    }

    /**
     * 模拟一个 *同步* 的本地兜底配置
     */
    private static Config getLocalDefaultConfig() {
        log("警告：无法获取主配置，正在加载本地 [默认] 配置...");
        return new Config("jdbc:h2:mem:fallback_db", 5);
    }


    // ----------------------------------------------------------------
    // 3. 主流程 (Main)
    // ----------------------------------------------------------------
    public static void main(String[] args) {
        log("主线程开始运行...");

        ExecutorService ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        CompletableFuture<Void> chain =

                // 步骤 1: 启动这个 *注定会失败* 的异步调用
                fetchConfigFromPrimary(ioExecutor)

                        // ================================================================
                        // 步骤 2: (这一步将被跳过)
                        // 这是一个 "成功路径" 回调。
                        // 因为上一步 (fetchConfigFromPrimary) 失败了，
                        // 链条会切换到 "异常路径"，这个 .thenApply 会被 *跳过*。
                        // ================================================================
                        .thenApply(config -> {
                            log("[成功路径] 严重：这个日志永远不应该被打印出来！");
                            return config;
                        })

                        // ================================================================
                        // 步骤 3: .exceptionally (异常捕获与恢复)
                        //
                        // 就像一个 try-catch 块。
                        // 链条发现自己处于 "异常路径"，于是它会执行 exceptionally 的函数。
                        //
                        // 1. 它接收 'ex' (即 RuntimeException) 作为参数。
                        // 2. 它执行函数体内的逻辑 (打印日志, 然后调用 getLocalDefaultConfig)。
                        // 3. 它 *必须* 返回一个与链条类型相符的 "兜底值" (即一个 Config 对象)。
                        // 4. 返回后，链条的状态被 "治愈"，切换回 "成功路径"。
                        // ================================================================
                        .exceptionally(ex -> {
                            log("[容错路径] 捕获到上游异常: " + ex.getMessage());
                            // 返回一个同步的兜底值
                            return getLocalDefaultConfig();
                        })

                        // ================================================================
                        // 步骤 4: 最终消费
                        //
                        // 因为 .exceptionally "治愈" 了链条，
                        // 这一步 .thenAccept 会被 *正常执行*。
                        // 它收到的 'config' 对象是来自 getLocalDefaultConfig() 的兜底值。
                        // ================================================================
                        .thenAccept(config -> {
                            log("[最终消费] 成功获取到配置。");
                            log("  -> 应用启动... 使用配置: " + config.databaseUrl());
                            log("  -> 连接池大小: " + config.connectionPoolSize());
                        });


        log("主线程：等待配置加载完成...");
        try {
            chain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log("主线程等待时出错: " + e.getMessage());
        } finally {
            ioExecutor.shutdown();
            log("主线程结束。");
        }
    }

    /**
     * 辅助方法：打印日志并带上线程名
     */
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
