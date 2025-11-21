package com.liboshuai.slr.engine.core.sink;

import com.liboshuai.slr.engine.model.RiskEvent;
import com.liboshuai.slr.engine.model.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Sink层：异步告警
 */
@Slf4j
@Component
public class AlertSink {

    @Resource
    @Qualifier("alertSinkExecutor")
    private ExecutorService sinkExecutor;

    public void sendAlert(RiskEvent event, RuleDefinition rule, double triggerValue) {
        // JUC 核心：异步执行，不阻塞主计算流程
        CompletableFuture.runAsync(() -> {
            doSend(event, rule, triggerValue);
        }, sinkExecutor).exceptionally(ex -> {
            log.error("Failed to send alert for rule {}", rule.getRuleId(), ex);
            return null;
        });
    }

    private void doSend(RiskEvent event, RuleDefinition rule, double triggerValue) {
        // 模拟写 MySQL 或调用 HTTP 接口
        log.warn("========== RISK ALERT DETECTED ==========");
        log.warn("Rule: {} ({})", rule.getRuleName(), rule.getRuleId());
        log.warn("Target: {}", event.getProperties().get(event.getTargetKey()));
        log.warn("Trigger Value: {}", triggerValue);
        log.warn("Threshold Script: {}", rule.getThresholdScript());
        log.warn("=========================================");

        // TODO: 实现 JDBC Template 写入 MySQL 告警表
        // TODO: 实现 RestTemplate 调用下游风控处理接口
    }
}