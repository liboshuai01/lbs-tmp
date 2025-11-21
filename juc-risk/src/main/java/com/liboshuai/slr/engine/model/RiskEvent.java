package com.liboshuai.slr.engine.model;

import lombok.Data;
import java.util.Map;

@Data
public class RiskEvent {
    private String eventId;
    private String userId;
    private String eventName;
    private long timestamp;
    private String ip;
    private Map<String, Object> properties;
}