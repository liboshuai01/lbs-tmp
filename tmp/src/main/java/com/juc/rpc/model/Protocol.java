package com.juc.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 协议层：定义交互数据格式
 */
public class Protocol {

    // JUC: 使用 AtomicLong 生成全局唯一的 RequestID，无锁高性能
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    public static long nextId() {
        return ID_GENERATOR.incrementAndGet();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RpcRequest implements Serializable {
        private Long requestId;
        private String className;
        private String methodName;
        private Class<?>[] paramTypes;
        private Object[] params;
        private long timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RpcResponse implements Serializable {
        private Long requestId;
        private Object result;
        private Throwable error;

        public boolean hasError() {
            return error != null;
        }
    }
}