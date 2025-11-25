package cn.liboshuai.jrisk.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置管理类
 * 负责统一创建和管理系统所需的各类线程池
 */
public class ThreadPoolConfig {

    /**
     * CPU 密集型线程池：用于核心规则计算
     * 核心线程数 = CPU核数 + 1
     * 队列：使用有界队列，防止 OOM
     */
    public static ThreadPoolExecutor getComputeExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cores + 1,
                cores + 1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2048), // 有界队列
                new NamedThreadFactory("risk-compute-"),
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者执行（实现简易反压）
        );
    }

    /**
     * IO 密集型线程池：用于 Redis/DB/RPC 异步查询
     * 核心线程数 = CPU核数 * 2 (或更多，取决于 IO 等待时间)
     */
    public static ThreadPoolExecutor getIoExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cores * 2,
                cores * 4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("risk-io-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 自定义线程工厂
     * JUC 最佳实践：为线程池中的线程命名，方便 jstack 排查问题
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false); // 设置为用户线程，防止主线程结束后立即退出
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}