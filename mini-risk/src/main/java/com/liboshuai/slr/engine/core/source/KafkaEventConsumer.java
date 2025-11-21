package com.liboshuai.slr.engine.core.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liboshuai.slr.engine.core.dispatcher.EventDispatcher;
import com.liboshuai.slr.engine.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Source层：多线程 Kafka 消费者
 * 替代 Flink Source
 */
@Slf4j
@Component
public class KafkaEventConsumer {

    @Value("${slr.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${slr.kafka.topic}")
    private String topic;
    @Value("${slr.kafka.group-id}")
    private String groupId;
    @Value("${slr.kafka.consumer-threads}")
    private int threadCount;

    @Resource
    private EventDispatcher eventDispatcher;

    private ExecutorService consumerExecutor;
    // JUC: AtomicBoolean 控制由于多线程环境下的启停状态
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void start() {
        // 创建固定大小的线程池来运行 Consumer Loop
        consumerExecutor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            consumerExecutor.submit(this::consumeLoop);
        }
        log.info("Kafka Consumer started with {} threads.", threadCount);
    }

    private void consumeLoop() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            while (isRunning.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            // 1. 反序列化
                            RiskEvent event = objectMapper.readValue(record.value(), RiskEvent.class);
                            // 2. 提交给分发器 (进入缓冲队列)
                            eventDispatcher.dispatch(event);
                        } catch (Exception e) {
                            log.error("Error parsing event: {}", record.value(), e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in consumer loop", e);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        isRunning.set(false);
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
        }
        log.info("Kafka Consumer shutdown initiated.");
    }
}