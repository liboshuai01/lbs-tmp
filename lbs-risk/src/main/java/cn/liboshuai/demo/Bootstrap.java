package cn.liboshuai.demo;

import cn.liboshuai.demo.core.RiskEngine;
import cn.liboshuai.demo.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        // 创建引擎实例
        RiskEngine engine = new RiskEngine();

        // 1. 初始化 (恢复状态)
        engine.init();

        // 2. 注册 Hook 实现优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("JVM Shutdown Hook triggered!");
            engine.shutdown();
        }));

        // 3. 启动
        engine.start();

        // 4. 模拟 Kafka 消费者线程
        Thread sourceThread = new Thread(() -> {
            log.info("Mock Source Started.");
            while (true) {
                try {
                    // 模拟生产速度：每 5ms 一条 (200 QPS)
                    Thread.sleep(5);

                    RiskEvent event = mockEvent();
                    engine.submitEvent(event);

                } catch (InterruptedException e) {
                    log.warn("Source thread interrupted");
                    break;
                }
            }
        });
        sourceThread.start();

        // 保持主线程不退出
        try {
            sourceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static RiskEvent mockEvent() {
        Map<String, Object> props = new HashMap<>();
        props.put("amount", ThreadLocalRandom.current().nextInt(100, 20000));

        // 模拟 10 个用户
        String userId = "user_" + ThreadLocalRandom.current().nextInt(1, 11);

        return RiskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .channel("APP")
                .eventName("PAYMENT")
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .properties(props)
                .build();
    }
}