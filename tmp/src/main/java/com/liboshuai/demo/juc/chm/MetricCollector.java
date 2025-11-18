package com.liboshuai.demo.juc.chm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;

// 导入 SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟 Flink Metrics 注册表，用于并发聚合 Counter。
 */
public class MetricCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricCollector.class);

    /**
     * 存储所有的 Counter 指标。
     * Key: Metric 名称 (例如 "totalRecordsIn")
     * Value: 聚合后的总数
     */
    private final Map<String, Long> counters = new ConcurrentHashMap<>();

    /**
     * 原子性地增加一个 Counter 的值。
     *
     * @param metricName Metric 的名称
     * @param value      本次要增加的值 (必须为正数)
     */
    public void incrementCounter(String metricName, long value) {
        if (value <= 0) {
            return;
        }

        // =========================================================================
        // TODO: 在这里实现你的原子聚合逻辑
        //
        // 需求：
        // 1. 原子性地将 'value' 累加到 'metricName' 对应的当前值上。
        // 2. 如果 'metricName' 在 Map 中不存在，它的 "当前值" 视为 0。
        // 3. 必须使用 JDK 1.8 ConcurrentHashMap 中最高效的 "聚合" 方法来实现。
        //
        // 提示：你需要一个专门为 "合并" (k, v) 而设计的方法。
        // =========================================================================

        // 示例：一个 *错误* 的、*非原子* 的实现 (请替换掉它)
        /*
        Long currentValue = counters.get(metricName);
        if (currentValue == null) {
            currentValue = 0L;
        }
        counters.put(metricName, currentValue + value); // <-- 非原子！高并发下会丢失计数
        */

        // 请将你的实现写在这里...
        counters.merge(metricName, value, Long::sum);
    }

    /**
     * 获取 Counter 的当前值
     */
    public Long getCounterValue(String metricName) {
        return counters.getOrDefault(metricName, 0L);
    }

    // =========================================================================
    // main 方法：模拟高并发聚合
    // =========================================================================
    public static void main(String[] args) throws InterruptedException {
        MetricCollector collector = new MetricCollector();

        // 模拟 Flink 8 路并行的 Task
        final int parallelism = 8;
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);

        final String metricName = "totalRecordsIn";
        // 8 个 Task，每个 Task 模拟处理 10000 次数据
        final int updatesPerTask = 10000;
        final long expectedTotal = (long) parallelism * updatesPerTask;

        log.info("模拟 {} 个并行 Task...", parallelism);
        log.info("每个 Task 更新 {} 次", updatesPerTask);
        log.info("预期的最终总数: {}", expectedTotal);

        for (int i = 0; i < parallelism; i++) {
            final int taskId = i;
            executor.submit(() -> {
                Random random = new Random(); // 只是为了增加一点随机性
                for (int j = 0; j < updatesPerTask; j++) {
                    // 模拟每次处理 1 条记录
                    collector.incrementCounter(metricName, 1);

                    // 稍微暂停一下，模拟真实的处理间隔
                    if (j % 1000 == 0) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(random.nextInt(5));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                log.info("Task-{} 完成.", taskId);
            });
        }

        // 等待所有 Task 完成
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // 检查最终结果
        long finalCount = collector.getCounterValue(metricName);

        log.info("========================================");
        log.info("所有 Task 完成.");
        log.info("聚合后的 '{}' 总数: {}", metricName, finalCount);
        log.info("预期的 '{}' 总数: {}", metricName, expectedTotal);
        log.info("========================================");

        if (finalCount == expectedTotal) {
            log.info("测试成功！计数准确无误。");
        } else {
            log.error("测试失败！发生了计数丢失或错误。");
        }
    }
}
