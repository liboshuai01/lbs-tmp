package cn.liboshuai.jrisk.rule;

import cn.liboshuai.jrisk.model.RiskContext;
import cn.liboshuai.jrisk.model.RiskResult;
import cn.liboshuai.jrisk.model.Rule;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则执行引擎
 * 封装 AviatorScript 执行逻辑
 */
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    /**
     * 执行单条规则
     * @param context 上下文 (包含 Event 和 特征数据)
     * @param rule 规则定义
     * @return 规则执行结果
     */
    public RiskResult execute(RiskContext context, Rule rule) {
        long start = System.nanoTime();
        boolean isTriggered = false;
        String errorMsg = null;

        try {
            // 1. 准备 Env (将上下文扁平化，方便脚本读取)
            Map<String, Object> env = new HashMap<>();
            env.put("event", context.getEvent());
            env.put("features", context.getFeatures());

            // 方便脚本直接写 amount 而不是 event.amount (可选)
            env.put("amount", context.getEvent().getAmount());
            env.put("userId", context.getEvent().getUserId());

            // 2. 编译并执行脚本
            // 注意: Aviator 会自动缓存编译好的 Expression，生产环境建议预编译
            Expression compiledExp = AviatorEvaluator.compile(rule.getScript());
            Object result = compiledExp.execute(env);

            if (result instanceof Boolean) {
                isTriggered = (Boolean) result;
            }

        } catch (Exception e) {
            log.error("Rule execution error: ruleId={}, script={}", rule.getRuleId(), rule.getScript(), e);
            errorMsg = e.getMessage();
        }

        long cost = (System.nanoTime() - start) / 1000; // 微秒

        return new RiskResult(
                rule.getRuleId(),
                rule.getRuleName(),
                isTriggered,
                isTriggered ? rule.getRiskLevel() : "PASS",
                isTriggered ? rule.getReason() : errorMsg,
                cost
        );
    }
}