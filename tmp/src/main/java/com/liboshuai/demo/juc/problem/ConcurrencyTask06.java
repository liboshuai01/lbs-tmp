package com.liboshuai.demo.juc.problem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 第六关：Semaphore 限流
 * 场景：Flink Sink 写入数据库，防止连接数爆满
 */
public class ConcurrencyTask06 {

    // 数据库只允许 3 个并发连接
    private static final int MAX_CONNECTIONS = 3;
    // Flink 任务并发度为 10
    private static final int TASK_COUNT = 10;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(TASK_COUNT);
        DatabaseConnector connector = new DatabaseConnector();

        for (int i = 0; i < TASK_COUNT; i++) {
            threadPool.execute(() -> {
                connector.connectAndQuery();
            });
        }

        threadPool.shutdown();
    }

    static class DatabaseConnector {
        // TODO: 任务 A - 定义 Semaphore
        // private final Semaphore semaphore = ...
        private final Semaphore semaphore = new Semaphore(3);

        public void connectAndQuery() {
            // TODO: 任务 B - 实现限流逻辑
            // 1. 获取许可 (acquire)
            // 2. 模拟数据库操作 (sleep)
            // 3. 释放许可 (release)
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {

                System.out.println(Thread.currentThread().getName() + " 尝试连接数据库...");

                // --- 模拟危险操作：如果没有限流，这里会瞬间涌入 10 个线程 ---
                simulateQuery();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 记得释放！
                semaphore.release();
            }
        }

        private void simulateQuery() {
            System.out.println(">>> " + Thread.currentThread().getName() + " 连接成功，正在查询...");
            try {
                // 模拟查询耗时 2秒
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("<<< " + Thread.currentThread().getName() + " 查询结束，断开连接。");
        }
    }

    /*
     * TODO: 思考题
     * 如果 release() 被多调用了一次，Semaphore 的许可数会超过初始值（3）变成 4 吗？
     * 答：会
     */
}
