package cn.liboshuai.demo.config;

import lombok.Data;

/**
 * 引擎配置类 (模拟从 properties/yaml 加载)
 */
@Data
public class EngineConfig {
    // 单例模式 (Lazy Initialization Holder Class 模式)
    private EngineConfig() {}
    private static class Holder { private static final EngineConfig INSTANCE = new EngineConfig(); }
    public static EngineConfig getInstance() { return Holder.INSTANCE; }

    // Redis Config
    private String redisHost = "127.0.0.1";
    private int redisPort = 6379;
    private int redisTimeout = 2000;
    private String stateKeyPrefix = "slr:state:"; // 状态存储前缀

    // Engine Config
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    private int queueCapacity = 20000;
    private long checkpointIntervalSeconds = 60; // 1分钟一次快照

    // Backpressure Config
    private int maxInFlightEvents = 50000; // 最大允许在途处理的事件数
}