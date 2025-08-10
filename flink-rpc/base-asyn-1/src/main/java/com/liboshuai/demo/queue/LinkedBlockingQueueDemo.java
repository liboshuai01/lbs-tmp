package com.liboshuai.demo.queue;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 模拟一个异步数据库客户端，它接受一个查询并返回一个 CompletableFuture。
 */
class AsyncDatabaseClient {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private final Random random = new Random();

    /**
     * 模拟异步查询，查询会随机耗时 100ms 到 600ms。
     * @param query 查询的 key
     * @return 一个持有未来结果的 CompletableFuture
     */
    public CompletableFuture<String> query(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟网络延迟和数据库处理时间
                int processingTime = 100 + random.nextInt(500);
                Thread.sleep(processingTime);
                return "Result for '" + query + "' (took " + processingTime + "ms)";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Failed to query '" + query + "'";
            }
        }, threadPool);
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}

/**
 * 模拟 Flink 的异步 I/O 算子。
 * 这就是 LinkedBlockingQueue 发挥作用的地方。
 */
class AsyncIOOperator implements Runnable {
    // LinkedBlockingQueue 作为异步结果的“信箱”或缓冲区。
    // Flink 的主任务线程将从这里获取已完成的 I/O 结果。
    // 在这个场景中，我们使用一个有界队列来模拟 Flink 的容量限制。
    private final BlockingQueue<String> resultQueue = new LinkedBlockingQueue<>(100);

    private final AsyncDatabaseClient asyncClient;
    private volatile boolean isRunning = true;

    // 用于向下游发送数据
    private final Consumer<String> downstream;

    public AsyncIOOperator(AsyncDatabaseClient asyncClient, Consumer<String> downstream) {
        this.asyncClient = asyncClient;
        this.downstream = downstream;
    }

    /**
     * 这个方法由 Flink 的主任务线程调用，用于处理上游来的数据。
     * @param input 来自上游的数据
     */
    public void processElement(String input) {
        System.out.println("[Operator Thread] Received input: " + input + ". Submitting async query.");

        // 1. 发起异步调用
        CompletableFuture<String> futureResult = asyncClient.query(input);

        // 2. 设置回调！当 Future 完成时，将结果放入 LinkedBlockingQueue。
        //    这个回调是由 AsyncDatabaseClient 的线程池执行的，不是主任务线程！
        futureResult.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // 错误处理
                try {
                    resultQueue.put("Error: " + throwable.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // 成功时将结果放入队列
                try {
                    // put() 是阻塞的，如果结果队列满了，I/O线程会等待。
                    // 这也形成了一种反压，防止过多的结果堆积。
                    resultQueue.put(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public void run() {
        System.out.println("[Operator Thread] Started. Waiting for async results...");
        while (isRunning || !resultQueue.isEmpty()) {
            try {
                // 3. 主任务线程从队列中非阻塞地获取结果。
                //    poll() 方法会立即返回，如果队列为空则返回 null。
                //    这可以防止主线程在没有结果时被阻塞，可以继续做其他工作（例如处理计时器）。
                String completedResult = resultQueue.poll(100, TimeUnit.MILLISECONDS);

                if (completedResult != null) {
                    // 4. 将获取到的结果发送到下游
                    downstream.accept(completedResult);
                }
                // 如果 completedResult 为 null，循环会继续，主线程不会卡住。

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[Operator Thread] Interrupted.");
            }
        }
        System.out.println("[Operator Thread] Stopped.");
    }

    public void stop() {
        isRunning = false;
    }
}


public class LinkedBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        AsyncDatabaseClient client = new AsyncDatabaseClient();

        // 模拟 Flink 的下游处理逻辑（这里只是简单打印）
        Consumer<String> downstreamConsumer = result ->
                System.out.println("[Downstream] <<<< " + result);

        // 创建并启动我们的模拟算子
        AsyncIOOperator operator = new AsyncIOOperator(client, downstreamConsumer);
        Thread operatorThread = new Thread(operator);
        operatorThread.start();

        // 模拟 Flink 从上游接收数据流
        for (int i = 0; i < 20; i++) {
            String inputData = "Key-" + i;
            operator.processElement(inputData);
            // 模拟数据到达的间隔
            Thread.sleep(200);
        }

        // 等待一段时间，让所有异步请求有机会完成
        System.out.println("\n=============================================");
        System.out.println("All inputs have been processed. Waiting for pending async operations to complete...");
        System.out.println("=============================================\n");

        Thread.sleep(5000);

        // 停止算子
        operator.stop();

        // 等待算子线程优雅地关闭
        operatorThread.join();
        client.shutdown();

        System.out.println("Demo finished.");
    }
}