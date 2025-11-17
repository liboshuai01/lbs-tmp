package com.liboshuai.demo.juc.chm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class Demo3 {

    private static final Logger log = LoggerFactory.getLogger(Demo3.class);

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("Flink", "Stateful-Computation");
        map.put("Spark", "In-Memory");

        // --- 1. replace(K key V Value) ---
        log.info("");
        log.info("--- 1. replace(K, V) ---");

        // 尝试替换存在的 key "Flink"
        String prevValue1 = map.replace("Flink", "Stream-Processing");
        log.info("替换 'Flink', 返回的旧值: {}", prevValue1); // Stateful-Computation
        log.info("Map 内容: {}", map); // {Spark=In-Memory, Flink=Stream-Processing}

        // 尝试替换不存在的 key "Kafka"
        String prevValue2 = map.replace("Kafka", "Messaging");
        log.info("");
        log.info("替换 'Kafka' (不存在), 返回的旧值: {}", prevValue2); // null
        log.info("Map 内容 (未改变): {}", map); // {Spark=In-Memory, Flink=Stream-Processing}

        // --- 2. replace(K key, V oldValue, V newValue) ---
        log.info("");
        log.info("--- 2. replace(K key, V oldValue, V newValue) ---");

        // 尝试替换, 但 oldValue 错误
        boolean success1 = map.replace("Spark", "Wrong-Old-Value", "Structured-Streaming");
        log.info("尝试替换 'Spark' (oldValue) 错误, 是否成功: {}", success1); // false
        log.info("Map 内容 (未改变): {}", map); // {Spark=In-Memory, Flink=Stream-Processing}

        // 尝试替换, oldValue 正确
        boolean success2 = map.replace("Spark", "In-Memory", "Structured-Streaming");
        log.info("");
        log.info("尝试替换 'Spark' (oldValue) 正确, 是否成功: {}", success2); // In-Memory
        log.info("Map 内容 (已更新): {}", map); // {Spark=Structured-Streaming, Flink=Stream-Processing}
    }
}
