package com.liboshuai.slr.engine.model;

import lombok.Data;

/**
 * 规则元数据
 * 对应 MySQL 中的 rule_info 等表结构
 */
@Data
public class RuleDefinition {
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 匹配渠道
     */
    private String channel;

    /**
     * 窗口大小 (秒)
     * 例如: 300 表示最近5分钟
     */
    private Integer windowSizeSeconds;

    /**
     * 聚合类型: COUNT, SUM, AVG
     */
    private String aggregatorType;

    /**
     * 聚合字段 (仅针对 SUM/AVG，如 amount)
     */
    private String aggregatorField;

    /**
     * 阈值判断表达式 (Aviator脚本)
     * 例如: "count > 5" 或 "sum_amount > 10000"
     */
    private String thresholdScript;

    /**
     * 告警级别
     */
    private String alertLevel;
}