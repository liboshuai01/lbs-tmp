package com.liboshuai.demo.juc.problem;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 模拟 Flink 的异步作业提交过程
 * 考察点：CompletableFuture 的组合、异常处理、非阻塞思维
 */
public class AsyncJobSubmitter {

    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(4, r -> new Thread(r, "IO-Thread"));
    private final ExecutorService rpcExecutor = Executors.newFixedThreadPool(4, r -> new Thread(r, "RPC-Thread"));

    // -------------------------------------------------------
    // 模拟三个耗时操作，它们本身已经是异步的，返回 CompletableFuture
    // -------------------------------------------------------

    public CompletableFuture<String> downloadJar() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            System.out.println(Thread.currentThread().getName() + ": Jar 包下载完成");
            return "FlinkJob.jar";
        }, ioExecutor);
    }

    public CompletableFuture<Integer> requestSlots() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(500);
            // 模拟偶尔资源不足失败
            if (Math.random() < 0.2) {
                throw new RuntimeException("资源不足！");
            }
            System.out.println(Thread.currentThread().getName() + ": 申请到 10 个 Slot");
            return 10;
        }, rpcExecutor);
    }

    public CompletableFuture<String> compileGraph(String jarPath) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + ": 开始编译 JobGraph (" + jarPath + ")");
            sleep(200); // 模拟编译耗时
            return "JobGraph-123";
        }, rpcExecutor);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /**
     * TODO: 这里的实现是极其错误的！它阻塞了主线程。
     * 请将其修改为纯异步实现。
     */
    public String submitJobBlocking() {
        try {
            // 1. 并行执行下载和申请资源
            CompletableFuture<String> jarFuture = downloadJar();
            CompletableFuture<Integer> slotFuture = requestSlots();

            // ❌ 错误：这里使用了 get()，导致阻塞！
            // 如果这是在 Flink 的 MainThread 里执行，整个节点就卡死了。
            String jarPath = jarFuture.get();
            Integer slots = slotFuture.get();

            // 2. 依赖上一步的结果进行编译
            CompletableFuture<String> graphFuture = compileGraph(jarPath);
            String graphId = graphFuture.get(); // ❌ 错误：再次阻塞

            return "作业提交成功: " + graphId + ", 使用 Slots: " + slots;

        } catch (InterruptedException | ExecutionException e) {
            return "作业提交失败: " + e.getMessage();
        }
    }

    /**
     * TODO: 请实现这个方法
     * 要求：
     * 1. downloadJar 和 requestSlots 必须并行运行。
     * 2. 当 downloadJar 完成后，才能调用 compileGraph。
     * 3. requestSlots 的结果需要保留，最后一起返回。
     * 4. 整个链条中不能出现 .get() 或 .join() 等阻塞操作。
     * 5. 任何步骤出错，最终结果应该是 Exception。
     */
    public CompletableFuture<String> submitJobAsync() {
        // 请在此处编写代码
        CompletableFuture<String> cf1 = downloadJar()
                .thenCompose(this::compileGraph);
        CompletableFuture<Integer> cf2 = requestSlots();
        return cf1.thenCombine(cf2, (String graphId, Integer slots) -> "作业提交成功: " + graphId + ", 使用 Slots: " + slots);
    }

    // -------------------------------------------------------
    // 测试入口
    // -------------------------------------------------------
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AsyncJobSubmitter submitter = new AsyncJobSubmitter();

        System.out.println("Main: 开始提交作业...");

        // 调用异步方法
        CompletableFuture<String> resultFuture = submitter.submitJobAsync();

        System.out.println("Main: 提交方法已返回（非阻塞），主线程可以做别的事...");

        // 在这里阻塞仅仅是为了等待程序不退出，观察结果
        // 在实际 Flink 中，这里会通过回调通知客户端
        resultFuture.whenComplete((res, ex) -> {
            if (ex != null) {
                System.err.println("【回调通知】提交最终失败: " + ex.getMessage());
            } else {
                System.out.println("【回调通知】" + res);
            }
        }).join(); // 仅用于 main 防止退出

        submitter.ioExecutor.shutdown();
        submitter.rpcExecutor.shutdown();
    }
}
