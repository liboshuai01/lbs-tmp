package com.liboshuai.slr.engine.core.engine;

import com.googlecode.aviator.AviatorEvaluator;
import com.liboshuai.slr.engine.core.sink.AlertSink;
import com.liboshuai.slr.engine.core.state.WindowStateManager;
import com.liboshuai.slr.engine.model.RiskEvent;
import com.liboshuai.slr.engine.model.RuleDefinition;
import com.liboshuai.slr.engine.service.RuleMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 核心计算引擎
 */
@Slf4j
@Component
public class RiskEngine {

    @Resource
    private RuleMetaService ruleMetaService; // 获取规则列表的服务

    @Resource
    private WindowStateManager stateManager;

    @Resource
    private AlertSink alertSink;

    /**
     * 处理单条事件
     * 此方法由 ruleComputeExecutor 线程池调用
     */
    public void process(RiskEvent event) {
        // 1. 获取适用于该事件渠道的规则列表
        // JUC注意: ruleList 需要是 CopyOnWriteArrayList 或 volatile 引用，支持热更新
        List<RuleDefinition> rules = ruleMetaService.getRulesByChannel(event.getChannel());

        if (rules == null || rules.isEmpty()) {
            return;
        }

        for (RuleDefinition rule : rules) {
            processRule(event, rule);
        }
    }

    private void processRule(RiskEvent event, RuleDefinition rule) {
        try {
            // 1. 解析事件中的数值 (如果是SUM/AVG类型)
            double eventValue = 0.0;
            if ("SUM".equalsIgnoreCase(rule.getAggregatorType())) {
                Object val = event.getProperties().get(rule.getAggregatorField());
                if (val instanceof Number) {
                    eventValue = ((Number) val).doubleValue();
                }
            } else {
                eventValue = 1.0; // COUNT
            }

            // 2. 更新状态 (Window State)
            String targetValue = (String) event.getProperties().get(event.getTargetKey());
            if (targetValue == null) return;

            stateManager.addEvent(rule.getRuleId(), targetValue, event.getTimestamp(), eventValue, rule.getWindowSizeSeconds());

            // 3. 获取当前窗口聚合结果
            double windowValue = stateManager.getAggregatedValue(rule.getRuleId(), targetValue, rule.getAggregatorType(), rule.getWindowSizeSeconds());

            // 4. 执行 Aviator 表达式判断
            // 构造上下文
            Map<String, Object> env = new HashMap<>();
            env.put("count", windowValue); // 假设脚本里写的是 count > 5
            env.put("sum", windowValue);   // 或者 sum > 1000

            boolean isHit = (Boolean) AviatorEvaluator.execute(rule.getThresholdScript(), env);

            if (isHit) {
                // 5. 命中规则，异步下发
                alertSink.sendAlert(event, rule, windowValue);
            }

        } catch (Exception e) {
            log.error("Error processing rule {} for event {}", rule.getRuleId(), event.getEventId(), e);
        }
    }
}