package cn.liboshuai.jrisk.core.event;

import java.io.Serializable;
import java.util.UUID;

/**
 * 基础事件抽象类
 */
public abstract class BaseEvent implements Serializable {

    private final String eventId;
    private final long timestamp;

    public BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventId() {
        return eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}