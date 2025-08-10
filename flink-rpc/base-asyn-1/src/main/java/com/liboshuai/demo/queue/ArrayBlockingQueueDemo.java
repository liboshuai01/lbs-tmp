package com.liboshuai.demo.queue;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueueDemo  {

    public static void main(String[] args) {
        ArrayBlockingQueue<LogMessage> queue = new ArrayBlockingQueue<>(5);
        LogProducer logProducer = new LogProducer(queue);
        LogConsumer logConsumer = new LogConsumer(queue);
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        try {
            threadPool.execute(logProducer);
            threadPool.execute(logConsumer);
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("\n\n shutting down the application...");
            // 发出停止信号
            logProducer.close();
            logConsumer.close();
            // 关闭线程池
            shutdownAndAwaitTermination(threadPool);
            System.out.println("✅ 应用程序已完全关闭。");
        }
    }

    // 来自官方文档的优雅关闭线程池的方法
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // 禁止提交新任务
        try {
            // 等待60秒，让现有任务执行完毕
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // 取消当前正在执行的任务
                // 再次等待60秒
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("线程池未能终止");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static class LogMessage {
        private final String message;

        LogMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "LogMessage{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }

    static class LogProducer implements Runnable {

        private boolean running = true;
        private final ArrayBlockingQueue<LogMessage> queue;

        public LogProducer(ArrayBlockingQueue<LogMessage> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                System.out.println("✅ 生产者启动...");
                while (running) {
                    // 模拟日志产生
                    String logContent = "Detail - " + UUID.randomUUID();
                    LogMessage logMessage = new LogMessage(logContent);
                    queue.put(logMessage);
                    System.out.println("➕ 生产了一条日志: " + logContent + " | 当前队列大小: " + queue.size());
                    // 模拟生产速率
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                System.out.println("❌ 生产者被中断。");
            } finally {
                System.out.println("🛑 生产者停止。");
            }
        }

        public void close() {
            running = false;
        }
    }

    static class LogConsumer implements Runnable {

        private boolean running = true;
        private final ArrayBlockingQueue<LogMessage> queue;

        public LogConsumer(ArrayBlockingQueue<LogMessage> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                System.out.println("✅ 消费者启动...");
                while (running || !queue.isEmpty()) {
                    LogMessage logMessage = queue.take();

                    // 模拟处理日志（例如：写入数据库）
                    System.out.println("➖ 消费了一条日志: " + logMessage.getMessage() + " | 当前队列大小: " + queue.size());
                    TimeUnit.MILLISECONDS.sleep(1500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                System.out.println("❌ 消费者被中断。");
            } finally {
                System.out.println("🛑 消费者停止。");
            }
        }

        public void close() {
            running = false;
        }
    }
}