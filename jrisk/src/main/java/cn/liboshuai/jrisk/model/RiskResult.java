package cn.liboshuai.jrisk.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskResult {
    private Long ruleId;
    private String ruleName;
    private boolean isTriggered;    // 是否命中
    private String riskLevel;       // 风险等级
    private String reason;          // 命中详情
    private long executionTime;     // 耗时(ms)
}