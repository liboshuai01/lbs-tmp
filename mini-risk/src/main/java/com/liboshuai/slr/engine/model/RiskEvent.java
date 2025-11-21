package com.liboshuai.slr.engine.model;

import lombok.Data;
import java.util.Map;

/**
 * 核心事件对象
 */
@Data
public class RiskEvent {
    /**
     * 事件唯一ID
     */
    private String eventId;

    /**
     * 事件发生时间戳 (Event Time)
     */
    private Long timestamp;

    /**
     * 渠道 (game, payment, login)
     */
    private String channel;

    /**
     * 目标字段 (如 userId, ip)
     */
    private String targetKey;

    /**
     * 具体的属性集合 (flattened json)
     */
    private Map<String, Object> properties;
}