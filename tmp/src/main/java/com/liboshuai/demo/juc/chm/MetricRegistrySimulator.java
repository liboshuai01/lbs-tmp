package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class MetricRegistrySimulator {

    // 存储指标名 -> 指标对象
    private final ConcurrentHashMap<String, Object> metrics = new ConcurrentHashMap<>();

    /**
     * 注册指标
     *
     * @param name     指标名称
     * @param newGauge 新创建的指标对象
     * @return 最终生效的指标对象 (可能是 newGauge，也可能是之前已存在的旧对象)
     */
    public Object registerGauge(String name, Object newGauge) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 如果 name 不存在，放入 newGauge
        // 2. 如果 name 已存在，**不要覆盖**
        // 3. 这是一个原子操作
        // 4. 返回最终在 Map 中的那个对象
        return metrics.compute(name, (key, oldValue) -> {
            Object gauge;
            if (oldValue == null) {
                gauge = newGauge;
            } else {
                gauge = oldValue;
            }
            return gauge;
        });
//        return null; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        MetricRegistrySimulator registry = new MetricRegistrySimulator();
        Object gauge1 = "Gauge-Version-1";
        Object gauge2 = "Gauge-Version-2";

        // 第一次注册，应该成功，返回 gauge1
        Object result1 = registry.registerGauge("my-metric", gauge1);
        System.out.println("First register: " + result1);

        // 第二次尝试注册同名指标，应该失败（不覆盖），并返回旧的 gauge1
        Object result2 = registry.registerGauge("my-metric", gauge2);
        System.out.println("Second register: " + result2);

        // 验证 Map 里确实还是 gauge1
        // ...
        System.out.println("Map: " + registry.metrics);
    }
}
