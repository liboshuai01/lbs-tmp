package com.liboshuai.slr.engine.core;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.liboshuai.slr.engine.model.RiskEvent;
import com.liboshuai.slr.engine.model.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RuleEngine {

    // 规则缓存：RuleID -> 编译后的表达式
    // 使用 ConcurrentHashMap 保证并发读安全
    private final Map<Long, Expression> expressionCache = new ConcurrentHashMap<>();

    // 规则元数据：volatile 保证可见性，用于热更新时的整体替换
    private volatile Map<Long, Rule> ruleMetadata = new HashMap<>();

    private final StateManager stateManager;

    public RuleEngine(StateManager stateManager) {
        this.stateManager = stateManager;
        // 初始化一些 Mock 规则
        initMockRules();
    }

    private void initMockRules() {
        // 示例规则：如果 60s 内访问次数 > 10，则拦截
        String script = "count > 10";
        Rule rule = new Rule(1L, "High Frequency Access", script, 10);
        reloadRule(rule);
    }

    /**
     * 动态加载/更新规则
     */
    public void reloadRule(Rule rule) {
        try {
            Expression compiledExp = AviatorEvaluator.compile(rule.getScript());
            expressionCache.put(rule.getId(), compiledExp);

            // Copy-On-Write 思想更新元数据
            Map<Long, Rule> newMeta = new HashMap<>(ruleMetadata);
            newMeta.put(rule.getId(), rule);
            ruleMetadata = newMeta;

            log.info("Rule loaded: {}", rule.getRuleName());
        } catch (Exception e) {
            log.error("Failed to compile rule script: " + rule.getScript(), e);
        }
    }

    /**
     * 执行规则匹配
     * @return true if matched (risk detected)
     */
    public boolean execute(RiskEvent event) {
        Map<Long, Rule> currentRules = ruleMetadata;

        // 准备上下文参数
        Map<String, Object> env = new HashMap<>();
        env.put("event", event);

        // 注入特征数据 (Data Enrichment)
        String stateKey = "user_freq_60s:" + event.getUserId();
        long count = stateManager.getCount(stateKey);
        env.put("count", count);

        for (Rule rule : currentRules.values()) {
            Expression expression = expressionCache.get(rule.getId());
            if (expression != null) {
                try {
                    Boolean result = (Boolean) expression.execute(env);
                    if (result) {
                        log.warn("Rule [{}] matched for user [{}]", rule.getRuleName(), event.getUserId());
                        return true; // 命中任意阻断规则即返回
                    }
                } catch (Exception e) {
                    log.error("Rule execution error id=" + rule.getId(), e);
                }
            }
        }
        return false;
    }
}