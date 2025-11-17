package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Demo2 {

    private static final Logger log = LoggerFactory.getLogger(Demo2.class);

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        // 1. 基本用法
        log.info("--- 1. 基本用法 ---");
        // 第一次尝试放入 "Flink"
        String oldValue1 = map.putIfAbsent("Flink", "Initial-Value-1");
        log.info("第一次放入 'Flink', 返回的旧值: " + oldValue1); // null
        log.info("Map 内容: " + map); // {Flink=Initial-Value-1}

        // 第二次尝试放入 "Flink" (key 已存在)
        String oldValue2 = map.putIfAbsent("Flink", "New-Value-2");
        log.info("");
        log.info("第二次放入 'Flink', 返回的旧值: {}", oldValue2); // Initial-Value-1
        log.info("Map 内容 (未改变): {}", map); // {Flink=Initial-Value-1}

        // 放入新的 key "Spark"
        String oldValue3 = map.putIfAbsent("Spark", "Spark-Value");
        log.info("");
        log.info("放入 'Spark', 返回的旧值: {}", oldValue3); // null
        log.info("Map 内容: {}", map); // {Flink=Initial-Value-1, Spark=Spark-Value}

        log.info("");
        log.info("--- 2. 并发场景演示 ---");
        // 模拟 10 个线程同时尝试为 "Job-1" 赋值
        // 只有一个线程能成功写入 "Job-1-Value-Thread-X"
        // 其他线程都会返回那个成功写入的值

        String key = "Job-1";
        ExecutorService pool = Executors.newFixedThreadPool(10);
        ConcurrentHashMap<String, String> concurrentMap = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> {
                        String valueToPut = "Job-1-Value-Thread-" + threadNum;
                        // 核心: 原子操作
                        String existingValue = concurrentMap.putIfAbsent(key, valueToPut);
                        if (existingValue == null) {
                            // existingValue 为 null, 说明当前线程是第一个成功放入的
                            log.info("线程 {} 成功放入值: {}", threadNum, valueToPut);
                        } else {
                            // existingValue 不为 null, 说明 key 已经存在
                            log.info("线程 {} 尝试放入失败, 已存在值: {}", threadNum, existingValue);
                        }
                    }
            ), pool);
            cfList.add(cf);
        }

        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allCf = CompletableFuture.allOf(cfArray);
        try {
            allCf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败!", e);
        } catch (TimeoutException e) {
            log.warn("超时! 在规定时间内并非所有任务都执行完毕!");
        }
        ExecutorUtils.close(pool, 10, TimeUnit.SECONDS);
    }
}
