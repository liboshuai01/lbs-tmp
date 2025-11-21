package com.liboshuai.slr.engine.mq;

import com.alibaba.fastjson2.JSON;
import com.liboshuai.slr.engine.core.JucPipeline;
import com.liboshuai.slr.engine.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumerRunner {

    private final JucPipeline pipeline;

    public KafkaConsumerRunner(JucPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Kafka 消费者入口
     * 单线程拉取，推送到内存队列，由后端线程池并发处理
     */
    @KafkaListener(topics = "slr_event_topic", groupId = "slr_juc_group_01", concurrency = "2")
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            String value = record.value();
            RiskEvent event = JSON.parseObject(value, RiskEvent.class);

            // 简单的数据清洗/补全
            if (event.getTimestamp() <= 0) {
                event.setTimestamp(System.currentTimeMillis());
            }

            // 推送至 JUC 管道
            pipeline.pushEvent(event);

        } catch (Exception e) {
            log.error("Failed to parse kafka message", e);
        }
    }
}