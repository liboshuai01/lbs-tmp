package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo5 {

    private static final Logger log = LoggerFactory.getLogger(Demo5.class);

    // 模拟一个昂贵的创建操作
    private static String createExpensiveResource(String key) throws InterruptedException {
        log.info("!!! 正在调用昂贵的创建操作来创建: {} !!!", key);
        TimeUnit.MILLISECONDS.sleep(500);
        return "Resource-For-" + key;
    }

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> resourceCache = new ConcurrentHashMap<>();

        // --- 1. 基本用法 (key 不存在) ---
        log.info("--- 1. 基本用法 (key 不存在) ---");
        // k -> createExpensiveResource(k), 这里 k 是 "Flink"
        // 因为 "Flink" 不存在, mappingFunction 会被调用
        String flinkResource = resourceCache.computeIfAbsent("Flink", FunctionUtils.uncheckedFunction(
                Demo5::createExpensiveResource
        ));
        log.info("获取到的 Flink 资源: {}", flinkResource); // 获取到的 Flink 资源: Resource-For-Flink
        log.info("缓存 Map 内容: {}", resourceCache); // 缓存 Map 内容: {Flink=Resource-For-Flink}

        // --- 2. 基本用法 (key 已存在) ---
        log.info("");
        log.info("--- 2. 基本用法 (key 已存在) ---");
        // 再次获取 "Flink"
        // 因为 "Flink" 已经存在, mappingFunction (createExpensiveResource) 不会被调用
        String flinkResourceCached = resourceCache.computeIfAbsent("Flink", FunctionUtils.uncheckedFunction(
                Demo5::createExpensiveResource
        ));
        log.info("再次获取 Flink 资源: {}", flinkResourceCached); // 再次获取 Flink 资源: Resource-For-Flink
        log.info("缓存 Map 内容 (未改变): {}", resourceCache); // 缓存 Map 内容 (未改变): {Flink=Resource-For-Flink}

        // --- 3. 并发场景: 懒加载缓存 ---
        log.info("");
        log.info("--- 3. 并发场景: 懒加载缓存 ---");
        // 10 个线程同时尝试获取 "Spark" 资源
        // 只有第一个到达的线程会执行 createExpensiveResource
        // 其他线程会等待它执行完毕, 然后获取它创建的值

        ExecutorService pool = Executors.newFixedThreadPool(10);
        String resourceKey = "Spark";

        // 用于记录创建操作被调用了多少次
        AtomicInteger creationCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> {
                        log.info("线程 {} 尝试获取 {}", threadNum, resourceKey);
                        String resource = resourceCache.computeIfAbsent(resourceKey, FunctionUtils.uncheckedFunction(
                                // 这部分代码 (mappingFunction) 是在锁内执行的
                                // 它只会被调用一次
                                k -> {
                                    creationCount.incrementAndGet();
                                    return Demo5.createExpensiveResource(k); // 调用昂贵操作
                                }
                        ));
                        log.info("线程 {} 成功获取到资源 {}", threadNum, resource);
                    }
            ), pool);
            cfList.add(cf);
        }
        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allCf = CompletableFuture.allOf(cfArray);
        try {
            allCf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
            log.info("昂贵的创建操作总共被调用了 {} 次。", creationCount.get()); // 昂贵的创建操作总共被调用了 1 次。
            log.info("缓存 Map 最终内容: " + resourceCache); // {Spark=Resource-For-Spark, Flink=Resource-For-Flink}
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
