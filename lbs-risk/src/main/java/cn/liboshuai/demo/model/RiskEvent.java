package cn.liboshuai.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 风控事件实体类
 * 对应 Flink 中的 EventDTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskEvent {
    private String eventId;
    private String channel;
    private String userId;
    private Long timestamp;
    private String eventName;
    private Map<String, Object> properties; // 动态属性
}