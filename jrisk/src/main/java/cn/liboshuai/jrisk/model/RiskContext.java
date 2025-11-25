package cn.liboshuai.jrisk.model;

import cn.liboshuai.jrisk.core.event.TradeEvent;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 风控上下文
 * 贯穿整个处理链路，承载 Event、外部画像数据、中间计算结果
 */
@Data
public class RiskContext {

    // 原始事件
    private final TradeEvent event;

    // 外部数据 (通过异步 IO 加载后填充到这里)
    // Key: DataSourceName (e.g., "UserProfile", "HistoryOrders"), Value: JSON/Object
    private final Map<String, Object> features = new ConcurrentHashMap<>();

    // 规则计算结果集
    // 使用 COW List，因为写少读多 (主要是最后汇总读)，且需要线程安全
    private final List<RiskResult> results = new CopyOnWriteArrayList<>();

    // 最终判定建议 (PASS, BLOCK, REVIEW)
    private volatile String finalDecision = "PASS";

    public RiskContext(TradeEvent event) {
        this.event = event;
    }

    /**
     * 添加特征数据 (线程安全)
     */
    public void addFeature(String key, Object value) {
        if (value != null) {
            features.put(key, value);
        }
    }

    /**
     * 记录规则命中结果 (线程安全)
     */
    public void addResult(RiskResult result) {
        results.add(result);
        // 简单的决策逻辑：只要有一个 REJECT，最终结果就是 REJECT
        if (result.isTriggered() && "REJECT".equals(result.getRiskLevel())) {
            this.finalDecision = "REJECT";
        }
    }
}