package com.liboshuai.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CompletableFutureQuickStartGuide {
    public static void main(String[] args) throws InterruptedException {
        // 场景1：提交一个异步任务，但是不关心异步任务执行的结果,例如异步日志落库
//        scene1_runAsync();

        // 场景2：提交一个异步任务，并获取异步任务的执行结果，然后对结果进行同步转换
//        scene2_runAsync();

        // 场景3：提交一个异步任务，并获取异步任务的执行结果，然后消费执行结果，最后不返回其他结果
//        scene3_runAsync();

        // 场景4：提交一个异步任务，并获取异步任务的执行结果，然后对结果进行异步操作
        scene4_runAsync();
    }

    /**
     * 场景4：提交一个异步任务，并获取异步任务的执行结果，然后对结果进行异步操作
     */
    private static void scene4_runAsync() throws InterruptedException {
        // 1. 提交一个异步任务，异步获取用户id
        // 2. 根据用户id再异步查看订单信息
        // 3. 最后主线程获取到对应于用户id的订单信息数据
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() ->
                        mockHttpRequest("查询用户id", 2), bizExecutor
                )
                .thenCompose(userId -> {
                    System.out.printf("线程 [%s] 根据用户id异步查看订单信息...%n", Thread.currentThread().getName());
                    return CompletableFuture.supplyAsync(() -> mockHttpRequest("查看用户订单信息", 2), bizExecutor);
                });
        TimeUnit.SECONDS.sleep(1);
        System.out.println("主线程继续执行其他任务......");
        // 等待所有异步任务执行完毕
        String result = completableFuture.join();
        System.out.println("主线程获取到最终结果: " + result);
        // 关闭业务线程池
        bizExecutor.shutdown();

        // 执行结果预测
        // 线程 [业务线程-23] 开始执行耗时操作: 查询用户id...%n
        // 主线程继续执行其他任务......
        // 线程 [业务线程-23] 已经完成耗时操作: 查询用户id...%n
        // 线程 [%s] 根据用户id异步查看订单信息...
        // 线程 [业务线程-24] 开始执行耗时操作: 查看用户订单信息...%n
        // 线程 [业务线程-24] 已经完成耗时操作: 查看用户订单信息...%n
        // 主线程获取到最终结果
    }

    /**
     * 场景3：提交一个异步任务，并获取异步任务的执行结果，然后消费执行结果，最后不返回其他结果
     */
    private static void scene3_runAsync() throws InterruptedException {
        // 1. 异步执行查看用户手机号的操作
        // 2. 根据查询到的用户手机号进行发短信操作（同步执行）
        // 3. 发完短信后，不产生任何结果
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> mockHttpRequest("查询用户手机号", 3), bizExecutor)
                .thenAccept(userPhone ->
                        System.out.printf("线程 [%s] 正在给用户 [%s] 发送短信...%n", Thread.currentThread().getName(), userPhone)
                );
        TimeUnit.SECONDS.sleep(1);
        System.out.println("主线程继续执行其他任务......");
        // 等待所有异步任务执行完毕
        completableFuture.join();
        // 关闭业务线程池
        bizExecutor.shutdown();

        // 线程 [%s] 开始执行耗时操作: %s...%n
        // 主线程继续执行其他任务......
        // 线程 [%s] 已经完成耗时操作: %s...%n
        // 线程 [%s] 正在给用户 [%s] 发送短信...%n
    }

    /**
     * 场景2：提交一个异步任务，并获取异步任务的执行结果，然后对结果进行同步转换
     * API-1: supplyAsync() 提交一个异步任务，并返回异步任务的结果
     * API-2: thenApply() 接收上一步的结果，并返回一个新的结果。同步执行。
     */
    private static void scene2_runAsync() throws InterruptedException {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(
                        () -> mockHttpRequest("查询指定用户信息", 2), bizExecutor)
                .thenApply(userInfo -> {
                    System.out.printf("线程 [%s] 正在加工用户信息...%n", Thread.currentThread().getName());
                    return String.format("加工后的用户信息 [%s]", userInfo);
                });
        TimeUnit.SECONDS.sleep(1);
        System.out.println("主线程继续执行其他任务......");
        // 注意：join 是一个阻塞方法，会一直等待这个 completableFuture 执行完所有操作，获得到结果，才会继续执行下面的代码
        String result = completableFuture.join();
        System.out.println("主线程获取到最终结果: " + result);
        // 关闭线程池
        bizExecutor.shutdown();

        // 线程 [业务线程-23] 开始执行耗时操作: 查询指定用户信息...
        // 主线程继续执行其他任务......
        // 线程 [业务线程-23] 已经完成耗时操作: 查询指定用户信息...
        // 线程 [业务线程-23] 正在加工用户信息...
        // 主线程获取到最终结果: 加工后的用户信息 ['查询指定用户信息' 的结果]
    }

    /**
     * 场景1：提交一个异步任务，但是不关心异步任务执行的结果,例如异步日志落库
     * API: runAsync(Runnable runnable,Executor executor) ，执行一个没有返回值的异步任务。
     */
    public static void scene1_runAsync() throws InterruptedException {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            mockHttpRequest("异步日志落库", 2);
        }, bizExecutor);
        TimeUnit.SECONDS.sleep(1);
        System.out.println("主线程继续执行其他任务......");
        // 等待所有异步任务执行完毕
        completableFuture.join();
        // 关闭业务线程池
        bizExecutor.shutdown();
    }

    /**
     * 模拟一个http请求所需要的线程池，生产环境不能这样做，需要自定义线程池的核心线程大小等等参数
     */
    private static final ExecutorService bizExecutor = Executors.newFixedThreadPool(10, r -> {
        Thread thread = new Thread(r);
        thread.setName("业务线程-" + thread.getId());
        return thread;
    });

    /**
     * 模拟http请求
     *
     * @param operationName 操作名称
     * @param seconds       操作所需时间（秒）
     * @return 操作结果
     */
    private static String mockHttpRequest(String operationName, int seconds) {
        try {
            System.out.printf("线程 [%s] 开始执行耗时操作: %s...%n", Thread.currentThread().getName(), operationName);
            TimeUnit.SECONDS.sleep(seconds);
            System.out.printf("线程 [%s] 已经完成耗时操作: %s...%n", Thread.currentThread().getName(), operationName);
            return String.format("'%s' 的结果", operationName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("模拟操作被中断", e);
        }
    }
}
