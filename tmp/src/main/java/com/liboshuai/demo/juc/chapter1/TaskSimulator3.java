package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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

    public static void main(String[] args) {
        StreamTask streamTask = new StreamTask();

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(streamTask, ioExecutor);
            completableFutures.add(completableFuture);
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.warn("主线程在休眠时被中断!");
            Thread.currentThread().interrupt();
        }
        streamTask.stop();
        CompletableFuture<?>[] completableFutureArray = completableFutures.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFutureArray);
        try {
            completableFuture.get(10, TimeUnit.SECONDS);
            log.info("所有任务全部在10秒内执行完毕!");
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败了!", e);
        } catch (TimeoutException e) {
            log.warn("超时! 所有任务未在10秒内全部完成");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        }
        log.info("所有线程共同处理了 " + streamTask.getCounter() + " 条数据");
        ExecutorUtil.close(ioExecutor, 10, TimeUnit.SECONDS);
    }
}
