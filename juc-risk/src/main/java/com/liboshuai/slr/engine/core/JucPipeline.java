package com.liboshuai.slr.engine.core;

import com.liboshuai.slr.engine.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JUC 核心处理管道
 * 架构：Kafka Consumer -> ArrayBlockingQueue (Buffer) -> Worker Threads (Rule Engine)
 */
@Slf4j
@Component
public class JucPipeline {

    // 1. 本地缓冲队列 (实现背压的关键)
    // 如果消费速度 > 生产速度，队列空，Worker等待。
    // 如果生产速度 > 消费速度，队列满，Consumer阻塞，从而减缓 Kafka 拉取。
    private final BlockingQueue<RiskEvent> bufferQueue = new ArrayBlockingQueue<>(5000);

    // 2. 核心业务线程池 (CPU 密集型)
    private ThreadPoolExecutor workerPool;

    // 运行标志位
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final RuleEngine ruleEngine;
    private final StateManager stateManager;

    public JucPipeline(RuleEngine ruleEngine, StateManager stateManager) {
        this.ruleEngine = ruleEngine;
        this.stateManager = stateManager;
    }

    @PostConstruct
    public void init() {
        // 初始化线程池
        int cores = Runtime.getRuntime().availableProcessors();
        workerPool = new ThreadPoolExecutor(
                cores, cores * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000), // 线程池内部队列
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "risk-worker-" + count++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由提交线程运行，变相实现降级/背压
        );

        // 启动 worker 消费线程
        new Thread(this::consumeLoop, "pipeline-dispatcher").start();
        log.info("Risk Engine Pipeline Started. Cores: {}", cores);
    }

    /**
     * Kafka 监听器调用此方法将数据推入管道
     */
    public void pushEvent(RiskEvent event) {
        if (!isRunning.get()) {
            log.warn("Engine is shutting down, rejecting event: {}", event);
            return;
        }
        try {
            // put 是阻塞方法，队列满时会阻塞 Kafka Consumer 线程，实现背压
            bufferQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Push event interrupted", e);
        }
    }

    /**
     * 内部循环：从 Buffer 取数据提交给 Worker
     */
    private void consumeLoop() {
        while (isRunning.get() || !bufferQueue.isEmpty()) {
            try {
                // Poll with timeout to allow checking isRunning flag periodically
                RiskEvent event = bufferQueue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    // 提交给线程池进行异步计算
                    CompletableFuture.runAsync(() -> process(event), workerPool)
                            .exceptionally(ex -> {
                                log.error("Rule execution failed", ex);
                                return null;
                            });
                }
            } catch (InterruptedException e) {
                log.warn("Dispatcher thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 单个事件的核心处理逻辑
     */
    private void process(RiskEvent event) {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 更新状态 (窗口计数)
            stateManager.updateState(event);

            // 2. 执行规则
            boolean isRisk = ruleEngine.execute(event);

            // 3. 结果处理
            if (isRisk) {
                log.warn("RISK DETECTED! Event: {}", event);
                // TODO: 发送告警到 Kafka 或 调用 RPC 拦截
            }
        } catch (Exception e) {
            log.error("Error processing event: " + event.getEventId(), e);
        } finally {
            // 简单的性能监控打印
            long cost = System.currentTimeMillis() - startTime;
            if (cost > 100) {
                log.warn("Slow processing event {}, cost {}ms", event.getEventId(), cost);
            }
        }
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void shutdown() {
        log.info("Starting graceful shutdown...");
        isRunning.set(false);

        // 1. 关闭线程池
        workerPool.shutdown();
        try {
            // 等待剩余任务执行（最长 10秒）
            if (!workerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }

        // 2. 触发状态落盘
        stateManager.flushAllStates();
        log.info("Shutdown complete.");
    }
}