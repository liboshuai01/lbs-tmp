package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Demo6 {

    private static final Logger log = LoggerFactory.getLogger(Demo6.class);

    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> counterMap = new ConcurrentHashMap<>();
        counterMap.put("Flink-Jobs", 5);
        counterMap.put("Spark-Jobs", 10);

        // --- 1. key 存在时 (更新) ---
        log.info("--- 1. key 存在时 (更新) ---");
        // (k, v) -> v + 1, 这里 k 是 "Flink-Jobs), v 是 5
        Integer newFlinkValue = counterMap.computeIfPresent("Flink-Jobs", (k, v) -> {
            log.info("computeIfPresent Flink: key={}, value={}", k, v); // computeIfPresent Flink: key=Flink-Jobs, value=5
            return v + 1;
        });
        log.info("computeIfPresent 返回的新值: {}", newFlinkValue); // computeIfPresent 返回的新值: 6
        log.info("Map 内容: {}", counterMap); // Map 内容: {Spark-Jobs=10, Flink-Jobs=6}

        // --- 2. key 不存在时 (无操作) ---
        log.info("");
        log.info("--- 2. key 不存在时 (无操作) ---");
        // "Kafka-Jobs" 不存在, Lambda 表达式不会执行
        Integer newKafkaValue = counterMap.computeIfPresent("Kafka-Jobs", (k, v) -> {
            log.info("computeIfPresent Kafka: key={}, value={}", k, v); // 这行代码不会执行
            return v + 1; // 这行代码不会执行
        });
        log.info("computeIfPresent 返回的新值: {}", newKafkaValue); // computeIfPresent 返回的新值: null
        log.info("Map 内容 (未改变): {}", counterMap); // Map 内容 (未改变): {Spark-Jobs=10, Flink-Jobs=6}

        // --- 3. key 存在时, 计算结果为 null (删除) ---
        log.info("");
        log.info("--- 3. key 存在时, 计算结果为 null (删除) ---");
        Integer newSparkValue = counterMap.computeIfPresent("Spark-Jobs", (k, v) -> {
            log.info("computeIfPresent Spark: key={}, value={}", k, v); // computeIfPresent Spark: key=Spark-Jobs, value=10
            return null;
        });
        log.info("computeIfPresent 返回的新值: {}", newSparkValue); // computeIfPresent 返回的新值: null
        log.info("Map 内容: {}", counterMap); // Map 内容: {Flink-Jobs=6}

        // --- 4. 并发场景: 更新已存在的计数器 ---
        log.info("");
        log.info("--- 4. 并发场景: 更新已存在的计数器 ---");
        // 假设 Flink-Jobs 必须已存在 (已初始化)
        ExecutorService pool = Executors.newFixedThreadPool(10);
        int numIncrements = 1000;

        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < numIncrements; i++) {
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> {
                        counterMap.computeIfPresent("Flink-Jobs", (k, v) -> v + 1);
                        counterMap.computeIfPresent("Hadoop-Jobs", (k, v) -> v + 1);
                    }
            ), pool);
            cfList.add(cf);
        }
        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allCf = CompletableFuture.allOf(cfArray);
        try {
            allCf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
            log.info("Flink-Jobs (初始为 6): " + counterMap.get("Flink-Jobs")); // Flink-Jobs (初始为 6): 1006
            log.info("Hadoop-Jobs (不存在): " + counterMap.get("Hadoop-Jobs")); // Hadoop-Jobs (不存在): null
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
