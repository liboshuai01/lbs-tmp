package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSimulator3 {
    
    private static final Logger log = LoggerFactory.getLogger(TaskSimulator3.class);
    
    static class StreamTask implements Runnable {

        private volatile boolean isRunning = true;

        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void run() {
            while (isRunning) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int count = counter.incrementAndGet();
                log.info("任务正在运行, Cnt: " + count);
            }
            log.info("收到停止信号, 退出循环。");
            log.info("总共处理了 " + counter.get() + " 条数据。");
        }

        public void stop() {
            log.info("正在发送停止信号...");
            isRunning = false;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        StreamTask streamTask = new StreamTask();

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(streamTask, ioExecutor);
            completableFutures.add(completableFuture);
        }

        TimeUnit.SECONDS.sleep(1);
        streamTask.stop();

        CompletableFuture<?>[] completableFutureArray = completableFutures.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFutureArray);
        completableFuture.join();

        log.info("确认任务已停止。");
        log.info("所有线程共同处理了 " + streamTask.getCounter() + " 条数据");

        ioExecutor.shutdown();
        log.info("已调用了 shutdown()");
        try {
            if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.error("任务在 10 秒内未能停止, 尝试强制关闭...");
                ioExecutor.shutdownNow();
                if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("线程池未能终止");
                }
            }
        } catch (InterruptedException e) {
            log.error("主线程在等待时被终端, 强制关闭线程池");
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
