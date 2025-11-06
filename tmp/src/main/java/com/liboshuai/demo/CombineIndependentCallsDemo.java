package com.liboshuai.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture.thenCombine()
 * 场景：组合两个 *相互独立* 的异步调用。
 * 1. 异步获取 网站访问者统计 (A)
 * 2. 异步获取 昨日销售总额 (B)
 * 3. 当 A 和 B *都* 完成后，将它们的结果合并为一个 Dashboard 对象。
 * 这与 Flink 源码中
 * jarUploadFuture.thenCombine(dispatcherGatewayFuture, ...)
 * 的逻辑完全相同：它必须 *同时* 等待 JAR 上传完毕 *和* Dispatcher Gateway 可用，
 * 然后才能将两者结合起来（通过 submitJob RPC）执行下一步。
 */
public class CombineIndependentCallsDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型
    // ----------------------------------------------------------------
    record VisitorStats(long uniqueVisitors, long pageViews) {}
    record SalesTotal(double totalAmount) {}
    // 最终合并的对象
    record Dashboard(VisitorStats stats, SalesTotal sales) {}

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”
    // ----------------------------------------------------------------

    /**
     * 模拟的API调用：异步获取访问者统计
     */
    public static CompletableFuture<VisitorStats> getVisitorStats(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("开始查询 访问者统计...");
                Thread.sleep(1000); // 模拟1秒的 I/O 延迟
                log("访问者统计 查询完毕。");
                return new VisitorStats(1500, 7500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * 模拟的API调用：异步获取销售总额
     */
    public static CompletableFuture<SalesTotal> getSalesTotal(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("开始查询 销售总额...");
                Thread.sleep(1500); // 模拟1.5秒的 I/O 延迟 (比上一个慢)
                log("销售总额 查询完毕。");
                return new SalesTotal(9870.50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    // ----------------------------------------------------------------
    // 3. 主流程 (Main)
    // ----------------------------------------------------------------
    public static void main(String[] args) {
        log("主线程开始运行...");

        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        log("同时启动两个异步任务...");

        // 步骤 1: 启动第一个 *独立* 任务
        CompletableFuture<VisitorStats> futureStats = getVisitorStats(ioExecutor);

        // 步骤 2: 启动第二个 *独立* 任务
        // (注意：这行代码不会等待 futureStats 完成，它会立即执行)
        CompletableFuture<SalesTotal> futureSales = getSalesTotal(ioExecutor);

        log("两个任务都已在后台并行运行...");

        // ================================================================
        // 核心演示：thenCombine
        // ================================================================
        CompletableFuture<Void> chain =

                // 步骤 3: .thenCombine (组合结果)
                //
                // futureStats.thenCombine(futureSales, ...)
                //
                // 告诉 CompletableFuture: "请等待 futureStats AND futureSales 都完成。"
                //
                // 当 *两者都* 成功时，
                // 1. 将 futureStats 的结果 (一个 VisitorStats 对象) 作为参数 'stats' 传入
                // 2. 将 futureSales 的结果 (一个 SalesTotal 对象) 作为参数 'sales' 传入
                // 3. 执行这个 BiFunction ( (stats, sales) -> ... )
                // 4. 将该函数的结果 (一个新的 Dashboard 对象) 包装到新的 Future 中
                //
                // 注意：合并函数 (BiFunction) 会在 *第二个任务完成* 所在的线程上执行。
                // (在这个例子中，是 getSalesTotal 所在的 io-executor 线程)
                // 如果合并操作很重，应使用 thenCombineAsync(..., ..., ioExecutor)
                // ================================================================
                futureStats.thenCombine(futureSales, (stats, sales) -> {
                            log("两个任务都已完成，开始合并数据...");
                            // 合并逻辑：创建一个新的 Dashboard 对象
                            return new Dashboard(stats, sales);
                        })

                        // ================================================================
                        // 步骤 4: 最终消费
                        //
                        // 这一步 .thenAccept 接收到的是合并后的 Dashboard 对象
                        // ================================================================
                        .thenAccept(dashboard -> {
                            log("=== 仪表盘加载成功 ===");
                            log("  访问者统计: " + dashboard.stats().uniqueVisitors() + " 独立访客");
                            log("  销售总额: ¥" + dashboard.sales().totalAmount());
                            log("=======================");
                        });

        log("主线程：等待合并任务完成...");
        try {
            // 阻塞主线程，等待整个链条执行完毕
            // 总耗时会是 *最慢* 的那个任务的耗时 (约1.5秒)，而不是两者相加
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
