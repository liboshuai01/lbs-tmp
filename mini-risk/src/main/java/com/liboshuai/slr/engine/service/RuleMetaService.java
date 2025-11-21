package com.liboshuai.slr.engine.service;

import com.liboshuai.slr.engine.model.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 规则管理服务
 * 支持规则热更新
 */
@Slf4j
@Service
public class RuleMetaService {

    // 缓存规则: Channel -> List<Rule>
    private Map<String, List<RuleDefinition>> ruleCache = new HashMap<>();

    // JUC: 读写锁，保证更新规则时，读取线程的安全性
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    @PostConstruct
    public void loadRules() {
        // 模拟从 MySQL 加载
        log.info("Loading rules from database...");
        Map<String, List<RuleDefinition>> newRules = new HashMap<>();

        // Mock 一个规则: 游戏渠道，统计最近5分钟(300s)内，同一UserId出现的次数 > 3
        RuleDefinition rule1 = new RuleDefinition();
        rule1.setRuleId(1001L);
        rule1.setRuleName("Game High Frequency Login");
        rule1.setChannel("game");
        rule1.setWindowSizeSeconds(300);
        rule1.setAggregatorType("COUNT");
        rule1.setThresholdScript("count > 3");
        rule1.setAlertLevel("HIGH");

        newRules.put("game", Collections.singletonList(rule1));

        // 获取写锁更新缓存
        rwLock.writeLock().lock();
        try {
            this.ruleCache = newRules;
        } finally {
            rwLock.writeLock().unlock();
        }
        log.info("Rules loaded successfully.");
    }

    /**
     * 获取规则列表 (高并发读)
     */
    public List<RuleDefinition> getRulesByChannel(String channel) {
        rwLock.readLock().lock();
        try {
            return ruleCache.getOrDefault(channel, Collections.emptyList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 提供给 Controller 调用的热刷新接口
     */
    public void refreshRules() {
        loadRules();
    }
}