package com.liboshuai.demo.cf;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture.thenCompose()
 *
 * 场景：处理有 *先后依赖关系* 的异步调用。
 * 1. 异步获取用户 User 对象。
 * 2. *然后* 根据 User 对象的 friendListId，再去异步获取 List<Friend>。
 *
 * 这种 "然后 (then)" 的关系是 `thenCompose` 的完美用例。
 */
public class DependentCallsDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型
    // ----------------------------------------------------------------
    // 用户简介，包含一个指向其好友列表的 ID
    record User(long id, String name, String friendListId) {}
    // 好友
    record Friend(long id, String name) {}

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”，注意这些方法 *自身* 就返回 CompletableFuture
    // ----------------------------------------------------------------

    /**
     * 模拟的API调用：异步获取用户简介
     * @param id 用户ID
     * @param executor 执行任务的线程池
     * @return 一个 CompletableFuture，它将在未来某个时刻完成并返回 User 对象
     */
    private static CompletableFuture<User> getUserProfile(long id, Executor executor) {
        // supplyAsync 会立即返回一个 Future，
        // 并在 executor 线程池中执行 lambda 表达式
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("开始查询用户简介 " + id + "...");
                Thread.sleep(1000); // 模拟1秒的 I/O 延迟
                log("用户简介查询完毕。");
                // 返回结果
                return new User(id, "Blink", "friends-list-xyz");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * 模拟的API调用：异步获取好友列表
     * @param friendListId 好友列表的ID (从 User 对象中获取)
     * @param executor 执行任务的线程池
     * @return 一个 CompletableFuture，它将在未来某个时刻完成并返回 List<Friend>
     */
    private static CompletableFuture<List<Friend>> getFriendList(String friendListId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("开始查询好友列表 " + friendListId + "...");
                Thread.sleep(1500); // 模拟1.5秒的 I/O 延迟
                log("好友列表查询完毕。");
                // 返回结果
                return List.of(
                        new Friend(201, "Alice"),
                        new Friend(202, "Bob")
                );
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

        long userId = 123L;

        // ================================================================
        // 核心演示：thenCompose
        // ================================================================

        CompletableFuture<Void> chain =
                // 步骤 1: 开始第一个异步调用
                getUserProfile(userId, ioExecutor)

                        // ================================================================
                        // 步骤 2: .thenCompose (编排依赖调用)
                        //
                        // 当 getUserProfile 完成后，会得到 User 对象。
                        // thenCompose 接收这个 User 对象，并 *返回一个新的 CompletableFuture*
                        // (即 getFriendList 的调用结果)。
                        //
                        // Flink 源码 (MiniCluster.submitJob) 中的
                        // .thenCombine(...).thenCompose(Function.identity())
                        // 也是在做类似的事情，它等待组合任务完成，然后 "压平" 结果。
                        // ================================================================
                        .thenCompose(user -> {
                            log("得到了用户: " + user.name() + "。现在开始获取好友列表...");

                            // **** 关键 ****
                            // 我们在这里返回了 *另一个* 异步调用。
                            // thenCompose 会 "解包" 或 "压平" 这个结果。
                            return getFriendList(user.friendListId(), ioExecutor);
                        })

                        // ================================================================
                        // 步骤 3: 最终消费
                        //
                        // 这一步 .thenAccept 接收到的是 List<Friend>，
                        // 而 *不是* CompletableFuture<List<Friend>>。
                        // 这就是 thenCompose "压平" 的功劳。
                        // ================================================================
                        .thenAccept(friends -> {
                            log("任务链成功完成！" + userId + " 的好友列表 (" + friends.size() + "人):");
                            // *** 已修复 ***：移除了错误的 "IE-" 字符
                            friends.forEach(f -> log("  -> " + f.name()));
                        });


        // (可选) 演示常见的 *错误* 做法
        // demonstratesWrongWay(userId, ioExecutor);

        log("主线程：已提交所有异步任务，等待它们完成...");
        try {
            // 阻塞主线程，等待整个链条执行完毕
            chain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log("主线程等待时出错: " + e.getMessage());
        } finally {
            ioExecutor.shutdown();
            log("主线程结束。");
        }
    }

    /**
     * 辅助方法：演示 *错误* 的做法 (使用 thenApply)
     */
    private static void demonstratesWrongWay(long userId, Executor ioExecutor) {
        log("[错误演示] 开始...");
        // 如果你错误地使用了 .thenApply
        CompletableFuture<CompletableFuture<List<Friend>>> nestedFuture =
                getUserProfile(userId, ioExecutor)
                        .thenApply(user -> {
                            // .thenApply 只是简单地 *包装* 你的返回值
                            // 因为 getFriendList 返回的是 CompletableFuture，
                            // 所以 .thenApply 的结果就是 CompletableFuture<CompletableFuture<...>>
                            return getFriendList(user.friendListId(), ioExecutor);
                        });

        log("[错误演示] 得到了一个嵌套的 Future，处理起来非常麻烦！");
        // 为了获取最终结果，你需要 .get().get() 或者丑陋的 .thenApply(f -> f.join())
    }


    /**
     * 辅助方法：打印日志并带上线程名
     */
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
