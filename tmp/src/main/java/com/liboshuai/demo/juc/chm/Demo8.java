package com.liboshuai.demo.juc.chm;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class Demo8 {

    private static final Logger log = LoggerFactory.getLogger(Demo8.class);

    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("Flink", 1);
        map.put("Spark", 1);
        map.put("Kafka", 1);
        map.put("Beam", 1);

        log.info("初始 Map: {}", map);

        log.info("");
        log.info("--- 1. 迭代时并发修改 (演示弱一致性) ---");

        // 创建一个线程, 它将迭代开始后修改 Map
        ExecutorService pool = Executors.newFixedThreadPool(1);
        CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                () -> {
                    // 等待 50 毫秒, 确保迭代器已经开始
                    TimeUnit.MILLISECONDS.sleep(50);
                    log.info("[Modifier Thread] 正在修改 Map...");

                    // 1. 添加一个新元素
                    map.put("Hadoop", 5);
                    log.info("[Modifier Thread] 添加了 'Hadoop'");

                    // 2. 删除一个已存在的元素
                    map.remove("Spark");
                    log.info("[Modifier Thread] 移除了 'Spark'");
                }
        ), pool);

        // (Main Thread) 开始迭代
        log.info("[Main Thread] 开始迭代 entrySet...");

        try {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                log.info("[Iterator] 遍历到: key={}, value={}", key, value);
                // 在迭代器中 sleep, 以确保 Modifier 线程有机会执行
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (Exception e) {
            log.error("迭代时发生意外错误: ", e);
        }

        log.info("[Main Thread] 迭代完成.");

        // 等待修改线程结束
        try {
            cf.get(10, TimeUnit.SECONDS);
            log.info("完成, 在规定时间内所有任务均执行完毕.");
            log.info("");
            log.info("迭代完成后的 Map 最终状态: {}", map); // 迭代完成后的 Map 最终状态: {Beam=1, Hadoop=5, Kafka=1, Flink=1}
            log.info("--- (请观察上面 'Hadoop' 和 'Spark' 在迭代中的表现) ---");
            log.info("--- (Hadoop 可能出现也可能不出现，Spark 可能被遍历到也可能不被遍历到) ---");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败!", e);
        } catch (TimeoutException e) {
            log.warn("超时! 在规定时间内并非所有任务都执行完毕!");
        }
        ExecutorUtils.close(pool, 10, TimeUnit.SECONDS);

        // --- 2. 迭代器 "支持" remove() ---
        log.info("");
        log.info("--- 2. 迭代器 '支持' remove() ---");
        Iterator<String> keyIterator = map.keySet().iterator();
        if (keyIterator.hasNext()) {
            String key = keyIterator.next();
            log.info("获取一个 key: {}", key); // 获取一个 key: Beam
            // 尝试通过迭代器删除
            keyIterator.remove();
        }
        log.info("最终 Map: {}", map); // 最终 Map: {Hadoop=5, Kafka=1, Flink=1}
    }
}
