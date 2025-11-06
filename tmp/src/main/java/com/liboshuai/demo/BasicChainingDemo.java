package com.liboshuai.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture 的基本链式调用：
 * supplyAsync (创建) -> thenApply/thenApplyAsync (转换) -> thenAccept (消费)
 *
 * 场景：模拟电商应用
 * 1. 异步根据用户ID获取 User 对象
 * 2. 异步根据 User 对象获取其订单 List<Order>
 * 3. 同步转换，计算订单数量
 * 4. 异步消费，打印最终结果
 */
public class BasicChainingDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型 (使用 Java 16+ 的 Records 简化)
    // ----------------------------------------------------------------
    record User(long id, String name) {}
    record Order(String id, long userId, String item) {}

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”，这些方法会模拟I/O延迟
    // ----------------------------------------------------------------

    /**
     * 模拟从数据库或API中获取用户
     */
    private static User fetchUser(long id) {
        try {
            log("开始查询用户 " + id + "...");
            Thread.sleep(1000); // 模拟1秒的数据库I/O延迟
            log("用户查询完毕。");
            return new User(id, "张三");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * 模拟根据用户获取其订单列表
     */
    private static List<Order> fetchOrders(User user) {
        try {
            log("开始查询 " + user.name() + " 的订单...");
            Thread.sleep(1500); // 模拟1.5秒的API调用延迟
            log("订单查询完毕。");
            return List.of(
                    new Order("order-123", user.id(), "笔记本电脑"),
                    new Order("order-456", user.id(), "机械键盘")
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------
    // 3. 主流程 (Main)
    // ----------------------------------------------------------------
    public static void main(String[] args) {
        log("主线程开始运行...");

        // 最佳实践：创建自定义线程池，专门用于I/O密集型任务
        // 避免使用 commonPool 来执行可能阻塞的操作
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true); // 设置为守护线程，以便JVM退出
            return t;
        });

        long userId = 101L;

        // 开始构建 CompletableFuture 链
        CompletableFuture<Void> processingChain =

                // ================================================================
                // 步骤 1: supplyAsync (创建任务)
                // 在 ioExecutor 线程池中异步执行 fetchUser 任务
                // ================================================================
                CompletableFuture.supplyAsync(() -> fetchUser(userId), ioExecutor)

                        // ================================================================
                        // 步骤 2: thenApplyAsync (异步转换)
                        // 当 fetchUser 完成后，会得到 User 对象。
                        // 这一步会 *将新任务(fetchOrders)提交到 ioExecutor*
                        // ================================================================
                        .thenApplyAsync(user -> fetchOrders(user), ioExecutor)

                        // ================================================================
                        // 步骤 3: thenApply (同步转换)
                        // 当 fetchOrders 完成后，会得到 List<Order>。
                        // 这一步 *不会切换线程*，它会 *在执行上一步(fetchOrders)的那个线程*
                        // (即 io-executor 线程) 上立即执行。
                        // 适用于非常轻量级的、非阻塞的转换。
                        // ================================================================
                        .thenApply(orders -> {
                            log("开始同步计算订单数量...");
                            return orders.size();
                        })

                        // ================================================================
                        // 步骤 4: thenAccept (异步消费)
                        // 当上一步(计算size)完成后，会得到订单数量 (Integer)。
                        // 这一步的 .thenAccept 没带 Async，所以它也会 *在 io-executor 线程*
                        // 上立即执行。它只是消费结果，没有返回值 (CompletableFuture<Void>)。
                        // ================================================================
                        .thenAccept(orderCount -> {
                            log("任务完成！" + userId + " 共有 " + orderCount + " 个订单。");
                        });

        log("主线程：已提交所有异步任务，等待它们完成...");

        // 因为 main 是非守护线程，而我们的异步链是守护线程，
        // 我们需要让主线程等待，否则JVM会立即退出
        try {
            // 阻塞主线程，等待整个链条执行完毕（或超时）
            processingChain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log("主线程等待时出错: " + e.getMessage());
        } finally {
            // 无论如何，关闭线程池
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