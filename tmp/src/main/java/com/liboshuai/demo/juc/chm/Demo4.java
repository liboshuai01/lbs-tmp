package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Demo4 {

    private static final Logger log = LoggerFactory.getLogger(Demo4.class);

    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("Flink", 10);
        map.put("Spark", 20);

        log.info("初始 Map: {}", map); // 初始 Map: {Spark=20, Flink=10}

        // --- 1. key 存在时, 更新值 ---
        // 演示: 将 "Flink" 的值 + 1
        log.info("");
        log.info("--- 1. key 存在时, 更新值 ---");
        // (k, v) -> v + 1 这里 k 是 "Flink", v 是 10
        Integer newFlinkValue = map.compute("Flink", (k, v) -> {
            log.info("compute Flink: key={}, value={}", k, v); // compute Flink: key=Flink, value=10
            return v == null ? 10 : v + 1;
        });
        log.info("compute 返回的新值: {}", newFlinkValue); // 11
        log.info("Map 内容: {}", map); // Map 内容: {Spark=20, Flink=11}

        // --- 2. key 不存在时, 创建值 ---
        log.info("");
        log.info("--- 2. key 不存在时, 创建值 ---");
        // (k, v) -> 100, 这里 k 是 "kafka", v 是 null
        Integer newKafkaValue = map.compute("Kafka", (k, v) -> {
            log.info("compute Kafka: key={}, value={}", k, v); // compute Kafka: key=Kafka, value=null
            return v == null ? 100 : v + 1;
        });
        log.info("compute 返回的新值: {}", newKafkaValue); // compute 返回的新值: 100
        log.info("Map 内容: {}", map); // Map 内容: {Kafka=100, Spark=20, Flink=11}

        // --- 3. key 存在时, 计算结果为 null (删除) ---
        // 演示: 删除 "Spark"
        log.info("");
        log.info("--- 3. key 存在时, 计算结果为 null (删除) ---");

        Integer newSparkValue = map.compute("Spark", (k, v) -> {
            log.info("compute Spark: key={}, value={}", k, v); // compute Spark: key=Spark, value=20
            return null; // 返回 null 会导致该 key 被移除
        });
        log.info("compute 返回的新值: {}", newSparkValue); // compute 返回的新值: null
        log.info("Map 内容: {}", map); // Map 内容: {Kafka=100, Flink=11}

        // --- 4. key 不存在时, 计算结果为 null (无操作) ---
        log.info("");
        log.info("--- 4. key 不存在时, 计算结果为 null (无操作) ---");
        Integer newHadoopValue = map.compute("Hadoop", (k, v) -> {
            log.info("compute Hadoop: key={}, value={}", k, v); // compute Hadoop: key=Hadoop, value=null
            return null; // key 不存在，返回 null，Map 不变
        });
        log.info("compute 返回的新值: {}", newHadoopValue); // compute 返回的新值: null
        log.info("Map 内容 (未改变): {}", map); // Map 内容 (未改变): {Kafka=100, Flink=11}

        // --- 5. 并发场景: 实现原子计数器 ---
        // 这是 compute 最强大的用途之一
        log.info("");
        log.info("--- 5. 并发场景: 实现原子计数器 ---");
        ConcurrentHashMap<String, Integer> counterMap = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        String counterKey = "Task-Success-Count";

        int numTasks = 1000;

        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> {
                        // 原子地 "读-改-写)
                        // (k, v) -> (v == null) ? 1 : v + 1
                        // 如果 v 是 null (第一次), 则设为 1
                        // 否则 (v 不是 null), 则设为 v + 1
                        counterMap.compute(counterKey, (k, v) -> v == null ? 1 : v + 1);
                    }
            ), pool);
            cfList.add(cf);
        }

        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfArray);
        try {
            cf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
            log.info("执行 {} 次并发增加后，计数器结果: {}", numTasks, counterMap.get(counterKey)); // 执行 1000 次并发增加后，计数器结果: 1000
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
