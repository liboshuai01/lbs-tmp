package cn.liboshuai.jrisk.core.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易事件 - 核心风控数据载体
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TradeEvent extends BaseEvent {

    // 核心维度
    private String userId;      // 用户ID
    private String orderId;     // 订单ID
    private BigDecimal amount;  // 交易金额
    private String tradeType;   // 交易类型 (如: RECHARGE, PAYMENT, WITHDRAW)

    // 环境维度
    private String ip;          // IP地址
    private String deviceId;    // 设备指纹
    private String location;    // 地理位置

    // 扩展字段 (用于存储非标准字段，避免频繁修改类结构)
    private Map<String, Object> properties = new HashMap<>();

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }
}