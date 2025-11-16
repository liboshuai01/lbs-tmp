package com.liboshuai.demo.juc.Chapter4;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicRaceConditionSimulator {

    private static final Logger log = LoggerFactory.getLogger(AtomicRaceConditionSimulator.class);

    public static void main(String[] args) {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        log.info("--- 实验 1: UnsafeCounter (long++) ---");
        runCounter(new UnsafeCounter(), threads, executor);

        log.info("--- 实验 2: AtomicCounter (AtomicLong) ---");
        runCounter(new SafeCounter(), threads, executor);

        ExecutorUtils.close(executor, 1, TimeUnit.MINUTES);
    }

    private static void runCounter(Counter counter, int threads, ExecutorService executor) {
        Instant start = Instant.now();

        int incrementsPerThread = 1_000_000;
        long expectedTotal = 10 * incrementsPerThread;

        log.info("开始模拟 " + threads + " 个线程, 每个执行 " + incrementsPerThread + " 次 'inc()'");
        log.info("预期总数: " + expectedTotal);

        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.inc();
                        }
                    }
            ), executor);
            cfList.add(cf);
        }
        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfArray);
        try {
            cf.get(1, TimeUnit.MINUTES);
            Instant end = Instant.now();
            long millis = Duration.between(start, end).toMillis();
            long finalCount = counter.get();

            log.info("模拟结束。");
            log.info("耗时: " + millis + " ms");
            log.info("最终计数: " + finalCount);

            if (finalCount == expectedTotal) {
                log.info("结果: 正确 (✓)");
            } else {
                log.error("结果: 错误 (✗) !!!!!!!!!!! 数据丢失了 " + (expectedTotal - finalCount) + " !!!!!!!!!!!");
            }
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败!");
        } catch (TimeoutException e) {
            log.warn("超时! 并非所有任务都在规定时间内执行完毕!");
        }
        log.info("==============================================\n");
    }

    private static interface Counter {
        void inc();

        long get();
    }

    private static class UnsafeCounter implements Counter {

        private long count = 0;

        @Override
        public void inc() {
            count++;
        }

        @Override
        public long get() {
            return count;
        }
    }

    private static class SafeCounter implements Counter {

        private final AtomicLong count = new AtomicLong(0);

        @Override
        public void inc() {
            count.incrementAndGet();
        }

        @Override
        public long get() {
            return count.get();
        }
    }

}
