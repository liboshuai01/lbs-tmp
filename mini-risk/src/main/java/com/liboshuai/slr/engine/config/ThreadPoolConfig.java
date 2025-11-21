package com.liboshuai.slr.engine.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * JUC 核心：自定义线程池管理
 * 实现了资源隔离，避免 IO 阻塞影响 CPU 计算
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Value("${slr.engine.worker-threads}")
    private int workerThreads;

    @Value("${slr.engine.sink-threads}")
    private int sinkThreads;

    /**
     * 1. 计算密集型线程池 (Worker Pool)
     * 用于执行规则匹配、状态更新。
     * 特点：线程数少（接近CPU核数），使用有界队列，拒绝策略为 CallerRuns（反压）。
     */
    @Bean("ruleComputeExecutor")
    public ThreadPoolExecutor ruleComputeExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("rule-compute-pool-%d").build();

        return new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(5000), // 有界队列，防止堆积OOM
                namedThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 核心反压机制：队列满了，由提交任务的线程（Source线程）自己执行，从而减慢消费速度
        );
    }

    /**
     * 2. IO密集型线程池 (Sink Pool)
     * 用于异步发送告警、写库。
     * 特点：线程数多，容忍一定的阻塞。
     */
    @Bean("alertSinkExecutor")
    public ExecutorService alertSinkExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("alert-sink-pool-%d").build();

        return new ThreadPoolExecutor(
                sinkThreads,
                sinkThreads * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                namedThreadFactory,
                new ThreadPoolExecutor.DiscardPolicy() // 极端情况下丢弃告警，保护系统主流程
        );
    }

    /**
     * 3. 调度线程池 (Scheduler)
     * 用于模拟 Flink 的 Window 触发器，定期清理过期状态
     */
    @Bean("windowCleanerExecutor")
    public ScheduledExecutorService windowCleanerExecutor() {
        return Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder().setNameFormat("window-cleaner-%d").build());
    }
}