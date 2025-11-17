package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class Demo7 {

    private static final Logger log = LoggerFactory.getLogger(Demo7.class);

    public static void main(String[] args) {
        // 场景: 统计单词频率
        ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<>();

        log.info("--- 1. key 不存在时 (merge 行为类似 put) ---");
        // key "Flink" 不存在
        // remappingFunction (v1, v2) -> v1 + v2 不会被调用
        // 传入的 value (1) 被直接放入 Map
        Integer newFlinkValue1 = wordCounts.merge("Flink", 1, (oldValue, value) -> {
            log.info("merge Flink: oldValue={}, value={}", oldValue, value); // 这行代码不会被执行
            return oldValue + value; // 这行代码不会被执行
        });
        log.info("merge 返回的新值: {}", newFlinkValue1); // merge 返回的新值: 1
        log.info("Map 内容: {}", wordCounts); // Map 内容: {Flink=1}

        log.info("");
        log.info("--- 2. key 存在时 (merge 执行合并) ---");
        // key "Flink" 存在 (oldValue=1)
        // 传入的 value 也是 1
        // remappingFunction 被调用: (1, 1) -> 2
        Integer newFlinkValue2 = wordCounts.merge("Flink", 1, (oldValue, value) -> {
            log.info("merge Flink: oldValue={}, value={}", oldValue, value); // merge Flink: oldValue=1, value=1
            return oldValue + value;
        });
        log.info("merge 返回的新值: {}", newFlinkValue2); // merge 返回的新值: 2
        log.info("Map 内容: {}", wordCounts); // Map 内容: {Flink=2}

        // 再添加一个 "Spark"
        Integer newSparkValue1 = wordCounts.merge("Spark", 1, Integer::sum);
        log.info("merge 返回的新值: {}", newSparkValue1); // merge 返回的新值: 1
        log.info("Map 内容: {}", wordCounts); // Map 内容: {Spark=1, Flink=2}

        // 再次 merge "Flink"
        Integer newFlinkValue3 = wordCounts.merge("Flink", 1, Integer::sum);
        log.info("merge 返回的新值: {}", newFlinkValue3); // merge 返回的新值: 3
        log.info("Map 内容: {}", wordCounts); // Map 内容: {Spark=1, Flink=3}

        log.info("");
        log.info("--- 3. merge 返回 null (删除) ---");
        // 假设我们用 merge 来 "减少" 计数
        // merge "Spark", 传入 value 为 -1
        // (oldValue, value) -> oldValue + -1, 这里的 oldValue = 1, value = -1
        // remappingFunction(1, -1) -> 0
        Integer newSparkValue2 = wordCounts.merge("Spark", -1, (oldValue, value) -> {
            int newValue = oldValue + value;
            return newValue == 0 ? null : newValue;
        });
        log.info("merge 返回的新值: {}", newSparkValue2); // merge 返回的新值: null
        log.info("Map 内容: {}", wordCounts); // Map 内容: {Flink=3}

        log.info("");
        log.info("--- 4. 并发场景: 并发词频统计 ---");
        ExecutorService pool = Executors.newFixedThreadPool(10);
        ConcurrentHashMap<String, Integer> concurrentCounts = new ConcurrentHashMap<>();
        String[] words = {"Flink", "Spark", "Flink", "Kafka", "Flink", "Spark", "Flink"};

        // 模拟多个线程同时提交单词
        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (String word : words) {
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                    () -> concurrentCounts.merge(word, 1, Integer::sum)
            ), pool);
            cfList.add(cf);
        }
        CompletableFuture<?>[] cfArray = cfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allCf = CompletableFuture.allOf(cfArray);
        try {
            allCf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
            log.info("并发词频统计结果: {}", concurrentCounts); // 并发词频统计结果: {Flink=4, Spark=2, Kafka=1}
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
