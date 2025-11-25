package cn.liboshuai.jrisk.rule;

import cn.liboshuai.jrisk.core.component.BaseComponent;
import cn.liboshuai.jrisk.model.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 规则管理器
 * 支持动态热更新 (Hot Reload)
 */
public class RuleManager extends BaseComponent {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    // 核心规则集合 (受读写锁保护)
    private List<Rule> ruleCache = new ArrayList<>();

    private ScheduledExecutorService scheduler;

    public RuleManager() {
        super("RuleManager");
    }

    @Override
    protected void doInit() {
        // 加载初始规则
        refreshRules();
    }

    @Override
    protected void doStart() {
        // 启动定时任务，模拟每 30 秒从数据库/配置中心拉取新规则
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshRules, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void doStop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * 获取当前生效的规则列表 (高并发读)
     */
    public List<Rule> getActiveRules() {
        readLock.lock();
        try {
            // 返回不可变视图，防止外部修改
            return Collections.unmodifiableList(new ArrayList<>(ruleCache));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 刷新规则 (低频写)
     * 模拟从 DB 加载
     */
    public void refreshRules() {
        log.info("Refreshing rules...");
        List<Rule> newRules = loadRulesFromMockDB();

        writeLock.lock(); // 获取写锁，此刻所有读线程会被阻塞
        try {
            this.ruleCache = newRules;
            log.info("Rules refreshed successfully. Total rules: {}", newRules.size());
        } finally {
            writeLock.unlock();
        }
    }

    private List<Rule> loadRulesFromMockDB() {
        List<Rule> rules = new ArrayList<>();

        // 规则 1: 大额交易拦截
        rules.add(Rule.builder()
                .ruleId(1001L)
                .ruleName("大额交易拦截")
                .script("amount > 900") // Aviator 表达式
                .priority(10)
                .riskLevel("REJECT")
                .reason("单笔交易金额超过阈值")
                .build());

        // 规则 2: 特定 IP 监控
        rules.add(Rule.builder()
                .ruleId(1002L)
                .ruleName("高危IP段监控")
                .script("string.startsWith(event.ip, '192.168.0')")
                .priority(5)
                .riskLevel("REVIEW")
                .reason("来自内网测试IP")
                .build());

        return rules;
    }
}