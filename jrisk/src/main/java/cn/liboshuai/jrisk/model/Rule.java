package cn.liboshuai.jrisk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule implements Serializable {

    private Long ruleId;            // 规则ID
    private String ruleName;        // 规则名称

    /**
     * Aviator 表达式脚本
     * 例如: "amount > 10000 && string.startsWith(ip, '192.168')"
     */
    private String script;

    private int priority;           // 优先级 (数值越高越先执行)
    private boolean enabled;        // 是否启用

    private String riskLevel;       // 风险等级 (REJECT, REVIEW, PASS)
    private String reason;          // 命中原因文案
}