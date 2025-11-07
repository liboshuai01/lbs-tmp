package com.liboshuai.demo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 演示 CompletableFuture.allOf()
 *
 * 场景：扇出/扇入 (Fan-out/Fan-in) 批量处理。
 * 1. (Fan-out) 收到一个文档列表，为 *每个* 文档启动一个 *并行* 的异步处理任务。
 * 2. (Fan-in) 等待 *所有* 的异步任务都执行完毕。
 * 3. (Final Action) 当全部完成后，收集所有结果并生成一个总结报告。
 */
public class FanOutFanInDemo {

    // ----------------------------------------------------------------
    // 1. 定义我们的数据模型
    // ----------------------------------------------------------------
    record Document(int id, String content) {}
    // 处理回执
    record ProcessingReceipt(int docId, String status, int processingTimeMs) {}

    private static final Random RANDOM = new Random();

    // ----------------------------------------------------------------
    // 2. 模拟的“服务层”
    // ----------------------------------------------------------------

    /**
     * 模拟的文档处理服务：
     * 它会花费一个随机时间来处理，并可能失败。
     */
    private static CompletableFuture<ProcessingReceipt> processDocumentAsync(
            Document doc, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            log("开始处理 [Doc " + doc.id() + "]...");
            int processingTimeMs = 500 + RANDOM.nextInt(1000); // 0.5s ~ 1.5s

            try {
                Thread.sleep(processingTimeMs);
            } catch (InterruptedException e) { /* ... */ }

            // 模拟一个随机的失败
            if (doc.content().contains("FAIL")) {
                log("处理 [Doc " + doc.id() + "]... 失败! (内容包含FAIL)");
                throw new RuntimeException("ProcessingError: Doc " + doc.id() + " is invalid");
            }

            log("完成处理 [Doc " + doc.id() + "]。 (" + processingTimeMs + "ms)");
            return new ProcessingReceipt(doc.id(), "Processed", processingTimeMs);
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
        // 演示 1: 成功的批量任务
        // ================================================================
        log("\n--- 开始演示 [成功] 的批量任务 ---");
        List<Document> successBatch = List.of(
                new Document(1, "content..."),
                new Document(2, "content..."),
                new Document(3, "content...")
        );
        runBatch(successBatch, ioExecutor);


        // ================================================================
        // 演示 2: 失败的批量任务 (一个任务会失败)
        // ================================================================
        log("\n--- 开始演示 [失败] 的批量任务 ---");
        List<Document> failingBatch = List.of(
                new Document(10, "content..."),
                new Document(11, "content... FAIL ..."), // 这个会失败
                new Document(12, "content...")
        );
        runBatch(failingBatch, ioExecutor);


        ioExecutor.shutdown();
        log("\n主线程结束。");
    }

    /**
     * 运行一个批量任务的辅助方法
     */
    private static void runBatch(List<Document> documents, Executor ioExecutor) {
        log("开始扇出 (Fan-out) " + documents.size() + " 个任务...");

        // 步骤 1: (Fan-out)
        // 为列表中的 *每个* 文档启动一个异步任务
        // 将所有返回的 CompletableFuture 收集到一个 List 中
        List<CompletableFuture<ProcessingReceipt>> futureList = documents.stream()
                .map(doc -> processDocumentAsync(doc, ioExecutor))
                .collect(Collectors.toList());

        // ================================================================
        // 核心演示：CompletableFuture.allOf()
        //
        // 1. 将 List<CompletableFuture> 转换为 CompletableFuture[]
        // 2. CompletableFuture.allOf() 接收这个数组
        // 3. 它返回一个 *新* 的 CompletableFuture<Void> (我们称之为 'allDoneFuture')
        // 4. 'allDoneFuture' 会在 *所有* 原始 Future 都完成后才完成
        // ================================================================
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(
                futureList.toArray(new CompletableFuture[0])
        );

        // 步骤 3: (Fan-in 和 Final Action)
        // 我们在 allDoneFuture 上注册一个回调
        CompletableFuture<Void> finalChain = allDoneFuture
                .thenAccept(v -> {
                    // *** 成功路径 ***
                    // 当 allOf() *成功* 完成时，这个块会执行
                    log("--- 批量任务 [成功] 完成! ---");
                    log("生成总结报告:");

                    // 此时，我们 *知道* allDoneFuture 已完成，
                    // 这意味着 futureList 中的 *所有* Future 也都已完成。
                    // 所以，我们现在调用 .join() 是 *安全* 的 (它不会阻塞)。
                    List<ProcessingReceipt> receipts = futureList.stream()
                            .map(CompletableFuture::join) // .join() 现在是安全的
                            .collect(Collectors.toList());

                    receipts.forEach(r ->
                            log("  -> [Doc " + r.docId() + "] 状态: " + r.status())
                    );
                })
                .exceptionally(ex -> {
                    // *** 异常路径 ***
                    // 如果 futureList 中 *任何一个* Future 失败了，
                    // .allOf() 会立即以该异常失败，
                    // .thenAccept() 会被 *跳过*，
                    // 而 .exceptionally() 会被执行。
                    log("--- 批量任务 [失败]! ---");
                    log("  -> 错误: " + ex.getCause().getMessage());

                    // 你仍然可以检查那些 *已经* 成功的任务
                    log("  -> 部分结果:");
                    for (CompletableFuture<ProcessingReceipt> future : futureList) {
                        if (future.isDone() && !future.isCompletedExceptionally()) {
                            log("    -> [Doc " + future.join().docId() + "] 已成功。");
                        }
                    }
                    return null; // 返回 Void
                });

        try {
            // 阻塞，直到 .thenAccept 或 .exceptionally 执行完毕
            finalChain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log("主线程等待时出错: " + e.getMessage());
        }
    }

    /** 辅助方法：打印日志并带上线程名 */
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
