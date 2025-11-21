package cn.liboshuai.demo.core.rule;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class RuleManager {

    private final AtomicReference<List<RiskRule>> currentRulesRef = new AtomicReference<>(new ArrayList<>());

    private final ScheduledExecutorService ruleRefresher = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Rule-Refresher-Thread");
        t.setDaemon(true);
        return t;
    });

    public void init() {
        reloadRules();
        ruleRefresher.scheduleAtFixedRate(this::reloadRules, 1, 1, TimeUnit.MINUTES);
    }

    public List<RiskRule> getRules() {
        return currentRulesRef.get();
    }

    private void reloadRules() {
        try {
            List<RiskRule> newRules = fetchRulesFromDB();
            for (RiskRule rule : newRules) {
                // 预编译，提升运行时性能
                Expression compiledExp = AviatorEvaluator.compile(rule.getExpression());
                rule.setCompiledExpression(compiledExp);
            }
            currentRulesRef.set(newRules);
            log.info("Rules reloaded. Count: {}", newRules.size());
        } catch (Exception e) {
            log.error("Failed to reload rules", e);
        }
    }

    private List<RiskRule> fetchRulesFromDB() {
        // Mock implementation
        List<RiskRule> rules = new ArrayList<>();
        rules.add(new RiskRule(1L, "High Frequency", "order_count_1min > 5"));
        rules.add(new RiskRule(2L, "Large Amount", "amount > 10000"));
        return rules;
    }

    @Data
    @AllArgsConstructor
    public static class RiskRule {
        private Long ruleId;
        private String ruleName;
        private String expression;
        private Expression compiledExpression;

        public RiskRule(Long ruleId, String ruleName, String expression) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
            this.expression = expression;
        }
    }
}