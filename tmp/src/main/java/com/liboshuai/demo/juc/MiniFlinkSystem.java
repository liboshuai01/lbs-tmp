package com.liboshuai.demo.juc;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MiniFlinkSystem {

    public static void main(String[] args) {
        // 1. 创建 Task 实例
        SourceStreamTask task = new SourceStreamTask();

        // 2. 启动 Task 线程 (模拟 TaskManager 中的 TaskSlot 线程)
        Thread taskThread = new Thread(() -> {
            try {
                task.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Task-Thread-1");
        taskThread.start();

        // 3. 模拟外部协调器 (Checkpoint Coordinator)
        // 使用 ScheduledExecutor 模拟定时触发 Checkpoint
        ScheduledExecutorService rpcEnv = Executors.newSingleThreadScheduledExecutor();

        // 延迟 500ms 后，每 600ms 触发一次 Checkpoint
        rpcEnv.scheduleAtFixedRate(new Runnable() {
            private long cpId = 1;
            @Override
            public void run() {
                long currentId = cpId++;
                System.out.println("\n[RPC-Thread] JobMaster 发起 Checkpoint " + currentId + " 请求...");

                // 这是一个异步调用，立即返回 Future
                CompletableFuture<Boolean> future = task.triggerCheckpoint(currentId);

                // 注册回调 (JUC 知识点)
                future.thenAccept(success -> {
                    System.out.println("[RPC-Thread] 收到 Checkpoint " + currentId + " 完成的 ACK.\n");
                });
            }
        }, 500, 600, TimeUnit.MILLISECONDS);

        // 4. 等待任务结束
        try {
            taskThread.join();
            rpcEnv.shutdown();
            // 稍微等待最后的 log 打印
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
