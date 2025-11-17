package com.liboshuai.demo.juc.chm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class Demo1 {

    private static final Logger log = LoggerFactory.getLogger(Demo1.class);

    public static void main(String[] args) {
        // 1. 创建一个 ConcurrentHashMap
        // Map<String, Integer> map = new ConcurrentHashMap<>();
        // 为了演示方便, 我们直接使用 ConcurrentHashMap 类型
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        log.info("--- 1. put 操作 ---");
        // 2. put 操作
        // put 方法是线程安全的. 在 JDK 1.8 中, 它通过 CAS 和synchronized 锁 (锁住桶的头节点)
        // 来保证并发写入的原子性, 而不是像 1.7 那样锁住整个 Segment.
        map.put("Flink", 1);
        map.put("Spark", 2);
        map.put("Kafka", 3);

        log.info("初始 Map 内容: " + map);

        log.info("");
        log.info("--- 2. get 操作 ---");
        // 3. get 操作
        // get 操作是线程安全的, 并且通常是非阻塞的 (无锁)
        // 由于 Java 内存模型 (JMM) 的保证, 一个线程的 put 操作对另一个线程的 get 操作是可见的.
        Integer flinkValue = map.get("Flink");
        log.info("获取 'Flink' 的值: " + flinkValue); // 1

        Integer nonExistentValue = map.get("Hadoop");
        log.info("获取 'Hadoop' (不存在的键) 的值: " + nonExistentValue); // null

        log.info("");
        log.info("--- 3. remove 操作 ---");
        // 4. remove 操作
        // remove 操作也是线程安全的, 使用与 put 类似的锁机制来保证原子性.
        Integer removedValue = map.remove("Spark");
        log.info("移除 'Spark', 被移除的值: " + removedValue); // 2

        Integer removedAgain = map.remove("Spark");
        log.info("再次移除 'Spark', 被移除的值: " + removedAgain); // null

        log.info("");
        log.info("---最终 Map 内容 ---");
        log.info("{}", map); // {Flink=1,Kafka=3}

        // 演示并发安全性 (简单模拟)
        // 实际的并发测试需要使用多线程
        log.info("");
        log.info("--- (演示) put 会覆盖旧值 ---");
        // put 操作会原子地覆盖旧值
        Integer oldValue = map.put("Flink", 118);
        log.info("更新 'Flink' 的值, 旧值是: " + oldValue); // 1
        log.info("更新后的 Map: " + map); // {Flink=118,Kafka=3}
    }
}
