package com.liboshuai.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 CompletableFuture.whenComplete()
 * 场景：模拟资源管理 (如数据库连接池)，保证资源 *必定* 被释放。
 * 1. 异步从池中获取一个连接 (Resource)。
 * 2. 异步使用该连接执行一个操作 (runQuery)。
 * 3. 使用 .whenComplete 确保 *无论* runQuery 成功还是失败，
 * 连接都会被释放 (release)。
 * 这与 Flink 源码中的逻辑高度一致：
 * - Flink 中：`new MiniClusterJobClient(...)` 可能会失败。
 * - Flink 中：.whenComplete((ignored, throwable) -> { ... })
 * - Flink 中：if (throwable != null) { shutDownCluster(miniCluster); }
 * Flink 的例子是 "如果创建失败，就关闭集群"。
 * 我们的例子是 "无论查询成功还是失败，都释放连接"。
 * 两者都使用 whenComplete 来执行 *副作用* (Side Effect) 和 *清理*。
 */
public class ResourceCleanupDemo {

    // ----------------------------------------------------------------
    // 1. 模拟我们的资源和连接池
    // ----------------------------------------------------------------

    /** 模拟一个昂贵的资源，比如数据库连接或网络套接字 */
    static class Connection {
        private final int id;
        private boolean isClosed = false;

        Connection(int id) { this.id = id; }

        public void close() {
            if (isClosed) return;
            log("释放 [连接 " + id + "]... 归还到池中。");
            isClosed = true;
        }

        public String query(String sql) {
            log("使用 [连接 " + id + "] 执行查询: " + sql);
            try {
                Thread.sleep(500); // 模拟查询I/O
            } catch (InterruptedException e) { /* ... */ }
            return "查询结果";
        }
    }

    /** 模拟连接池，并统计活动连接数 */
    static class ConnectionPool {
        private final AtomicInteger activeConnections = new AtomicInteger(0);

        public CompletableFuture<Connection> getConnection(Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                log("获取连接... (当前活动: " + activeConnections.incrementAndGet() + ")");
                return new Connection(activeConnections.get());
            }, executor);
        }

        // 注意：释放连接是 *同步* 的
        public void releaseConnection(Connection conn) {
            conn.close();
            activeConnections.decrementAndGet();
        }
    }

    // ----------------------------------------------------------------
    // 2. 主流程 (Main)
    // ----------------------------------------------------------------

    public static void main(String[] args) {
        log("主线程开始运行...");

        ExecutorService ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        ConnectionPool pool = new ConnectionPool();

        // ================================================================
        // 演示 1: 成功路径
        // ================================================================
        log("\n--- 开始演示 [成功] 路径 ---");
        CompletableFuture<String> successChain =
                pool.getConnection(ioExecutor) // 1. 获取连接
                        .thenCompose(connection -> // 2. 使用连接 (返回 Future<String>)
                                runQueryAsync(connection, "SELECT * FROM users", false, ioExecutor)

                                        // ================================================================
                                        // 核心演示: .whenComplete (我们的 "finally" 块)
                                        //
                                        // 1. .whenComplete 会 *透传* 上一步的结果 (String) 和 异常 (Throwable)。
                                        // 2. 它 *不关心* 上一步是成功还是失败，它 *总是* 会执行。
                                        // 3. 它的目的是执行 *副作用* (释放连接)，而不是转换结果。
                                        // ================================================================
                                        .whenComplete((result, exception) -> {
                                            // 此时，(result, exception) 是 ("查询结果", null)
                                            log("[whenComplete] 观察到操作完成。");

                                            // 无论如何都执行清理
                                            pool.releaseConnection(connection);
                                        })
                        );

        try {
            String result = successChain.get(2, TimeUnit.SECONDS);
            log("[成功] 演示的主线程收到最终结果: " + result);
        } catch (Exception e) {
            log("[成功] 演示的主线程捕获到异常: " + e.getMessage());
        }


        // ================================================================
        // 演示 2: 失败路径
        // ================================================================
        log("\n--- 开始演示 [失败] 路径 ---");
        CompletableFuture<String> failureChain =
                pool.getConnection(ioExecutor) // 1. 获取连接
                        .thenCompose(connection -> // 2. 使用连接，但这次 *强制失败*
                                runQueryAsync(connection, "DROP TABLE users", true, ioExecutor)

                                        // ================================================================
                                        // 核心演示: .whenComplete (我们的 "finally" 块)
                                        //
                                        // 这一次，.whenComplete 观察到了 *异常*
                                        // ================================================================
                                        .whenComplete((result, exception) -> {
                                            // 此时，(result, exception) 是 (null, QueryException)
                                            log("[whenComplete] 观察到操作完成。");

                                            // 关键：*即使* exception != null，清理逻辑 *仍然* 执行！
                                            pool.releaseConnection(connection);
                                        })
                        );

        try {
            String result = failureChain.get(2, TimeUnit.SECONDS);
            log("[失败] 演示的主线程收到最终结果: " + result);
        } catch (Exception e) {
            // 这是预期行为！异常被 whenComplete "观察" 后，继续被传递下来。
            log("[失败] 演示的主线程 *按预期* 捕获到异常: " + e.getCause().getMessage());
        }


        ioExecutor.shutdown();
        log("\n主线程结束。");
    }

    /**
     * 模拟一个异步查询，它可以被配置为成功或失败
     */
    public static CompletableFuture<String> runQueryAsync(
            Connection conn, String sql, boolean forceFailure, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            if (forceFailure) {
                log("使用 [连接 " + conn.id + "] 执行查询时 *失败*...");
                throw new RuntimeException("QueryException: SQL语法错误 '" + sql + "'");
            }
            // 正常执行
            return conn.query(sql);
        }, executor);
    }

    /** 辅助方法：打印日志并带上线程名 */
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
