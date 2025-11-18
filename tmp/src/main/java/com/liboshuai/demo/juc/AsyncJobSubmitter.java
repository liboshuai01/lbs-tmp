package com.liboshuai.demo.juc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AsyncJobSubmitter {

    // 模拟下载 Jar 包 (异步操作)
    private CompletableFuture<String> downloadJar(String jobName) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100); // 模拟耗时
            return "/tmp/" + jobName + ".jar";
        });
    }

    // 模拟校验 Jar 包 (同步操作，但也可能很慢)
    private String verifyJar(String path) {
        sleep(50);
        return path + " [VERIFIED]";
    }

    // 模拟启动 Job (返回一个新的 Future)
    private CompletableFuture<String> startJob(String verifiedPath) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return "Job Started with: " + verifiedPath;
        });
    }

    // ❌ 错误示范：阻塞式写法
    public String submitJobBlocking(String jobName) throws ExecutionException, InterruptedException {
        CompletableFuture<String> downloadFuture = downloadJar(jobName);
        String path = downloadFuture.get(); // 💣 阻塞！如果不返回，线程卡死

        String verified = verifyJar(path);

        CompletableFuture<String> startFuture = startJob(verified);
        return startFuture.get(); // 💣 再次阻塞！
    }

    // ✅ TODO: 请实现非阻塞版本
    // 提示：
    // 1. 当你需要拿到上一步的结果，做一些同步转换时，用 thenApply
    // 2. 当你需要拿到上一步的结果，然后发起一个新的异步操作（返回 Future）时，用什么？(flatMap 思想)
    public CompletableFuture<String> submitJobAsync(String jobName) {
        return downloadJar(jobName)
                // TODO: 第一步：拿到 path 后调用 verifyJar
                // .then???
                .thenApplyAsync(this::verifyJar)
                // TODO: 第二步：拿到 verifiedPath 后调用 startJob (注意 startJob 返回的是 Future)
                // .then???
                .thenCompose(this::startJob)
                ;
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }
}