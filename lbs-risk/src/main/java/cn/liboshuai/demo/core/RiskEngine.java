package cn.liboshuai.demo.core;

import cn.liboshuai.demo.config.EngineConfig;
import cn.liboshuai.demo.core.lifecycle.LifeCycle;
import cn.liboshuai.demo.core.rule.RuleManager;
import cn.liboshuai.demo.core.state.StateBackend;
import cn.liboshuai.demo.core.state.impl.RedisStateBackend;
import cn.liboshuai.demo.core.window.SlidingWindowCounter;
import cn.liboshuai.demo.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RiskEngine implements LifeCycle {

    private final EngineConfig config;

    // 组件
    private final RuleManager ruleManager;
    private final StateBackend stateBackend;

    // 核心数据结构
    // 1. 状态存储 (ConcurrentHashMap)
    private ConcurrentHashMap<String, SlidingWindowCounter> stateStore;

    // 2. 内部缓冲队列
    private final BlockingQueue<RiskEvent> eventQueue;

    // 3. 工作线程池
    private final ThreadPoolExecutor workerPool;

    // 4. 调度线程池 (用于 Checkpoint)
    private final ScheduledExecutorService scheduler;

    // 流量控制 (JUC Semaphore)
    // 用于控制“在途”事件数量，实现背压。Source 放入前 acquire，Worker 处理完 release。
    private final Semaphore backpressureLimiter;

    // 运行状态控制
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPausedForCheckpoint = new AtomicBoolean(false);

    // 监控指标 (JUC AtomicLong / LongAdder)
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public RiskEngine() {
        this.config = EngineConfig.getInstance();
        this.ruleManager = new RuleManager();
        this.stateBackend = new RedisStateBackend();

        this.eventQueue = new ArrayBlockingQueue<>(config.getQueueCapacity());
        this.backpressureLimiter = new Semaphore(config.getMaxInFlightEvents());

        // 自定义 ThreadFactory 方便 Dump 分析
        ThreadFactory workerThreadFactory = r -> {
            Thread t = new Thread(r);
            t.setName("Risk-Worker-" + t.getId());
            t.setUncaughtExceptionHandler((thread, e) -> log.error("Worker Thread Exception", e));
            return t;
        };

        // 拒绝策略：CallerRunsPolicy (让 Source 线程自己跑，进一步降低生产速度)
        this.workerPool = new ThreadPoolExecutor(
                config.getWorkerThreads(),
                config.getWorkerThreads(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(5000),
                workerThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Engine-Scheduler"));
    }

    @Override
    public void init() {
        log.info("Initializing Risk Engine...");
        // 1. 从 Redis 恢复状态
        this.stateStore = stateBackend.load();
        // 2. 初始化规则
        ruleManager.init();
    }

    @Override
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("Starting Risk Engine...");

            // 启动 Checkpoint 定时任务
            long interval = config.getCheckpointIntervalSeconds();
            scheduler.scheduleAtFixedRate(this::performCheckpoint, interval, interval, TimeUnit.SECONDS);

            // 启动消费分发线程 (Consumer -> Queue -> Dispatch)
            new Thread(this::dispatchLoop, "Dispatcher-Thread").start();

            log.info("Risk Engine Started. Initial State Size: {}", stateStore.size());
        }
    }

    /**
     * 外部调用入口 (模拟 Kafka Source 调用)
     */
    public void submitEvent(RiskEvent event) {
        if (!isRunning.get()) return;

        try {
            // 1. Checkpoint 期间暂停写入 (简单的 Stop-the-World 实现一致性)
            // 在高并发下，这里可以用 ReadWriteLock 的 ReadLock 优化，Checkpoint 用 WriteLock
            while (isPausedForCheckpoint.get()) {
                Thread.sleep(10);
            }

            // 2. 获取流控许可 (背压)
            backpressureLimiter.acquire();

            // 3. 入队
            if (!eventQueue.offer(event, 2, TimeUnit.SECONDS)) {
                // 队列满且超时，释放许可，记录丢弃或降级
                backpressureLimiter.release();
                log.warn("Queue full, event dropped: {}", event.getEventId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void dispatchLoop() {
        while (isRunning.get()) {
            try {
                // 只要队列有数据就处理
                RiskEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    // 异步提交给 Worker
                    CompletableFuture.runAsync(() -> processEvent(event), workerPool)
                            .whenComplete((res, ex) -> {
                                // 无论成功失败，都要释放许可
                                backpressureLimiter.release();
                                if (ex != null) {
                                    errorCount.incrementAndGet();
                                    log.error("Event processing failed", ex);
                                } else {
                                    processedCount.incrementAndGet();
                                }
                            });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processEvent(RiskEvent event) {
        // 1. 状态更新 (Thread-Safe via ConcurrentHashMap + StampedLock inside Window)
        String stateKey = event.getUserId() + ":order_count_1min";
        SlidingWindowCounter counter = stateStore.computeIfAbsent(stateKey,
                k -> new SlidingWindowCounter(60));
        counter.add(1);

        // 2. 规则计算
        Map<String, Object> env = new HashMap<>();
        env.put("userId", event.getUserId());
        env.put("amount", event.getProperties().getOrDefault("amount", 0));
        env.put("order_count_1min", counter.sum());

        List<RuleManager.RiskRule> rules = ruleManager.getRules();
        for (RuleManager.RiskRule rule : rules) {
            try {
                Boolean result = (Boolean) rule.getCompiledExpression().execute(env);
                if (result != null && result) {
                    log.warn(">>> ALERT: Rule [{}], User [{}], Val [{}]",
                            rule.getRuleName(), event.getUserId(), env.get("order_count_1min"));
                }
            } catch (Exception e) {
                // log.debug("Rule check failed", e);
            }
        }
    }

    /**
     * 执行一致性快照 (Checkpoint)
     * 策略：Pause Source -> Wait Queue Empty -> Snapshot -> Resume
     */
    private synchronized void performCheckpoint() {
        long start = System.currentTimeMillis();
        log.info("Starting Checkpoint...");

        // 1. 暂停 Source 写入
        isPausedForCheckpoint.set(true);

        try {
            // 2. 等待队列排空 (确保所有已提交的事件都更新到了 stateStore)
            // 为了防止死等，设置最大等待时间
            int retry = 0;
            while (!eventQueue.isEmpty() && retry < 50) {
                Thread.sleep(10);
                retry++;
            }

            // 3. 等待 Worker 线程池稍微空闲 (可选，这里简单处理，只要队列空了，大部分状态已更新)
            // 如果需要严格 Exactly-Once，这里需要更复杂的 Barrier 机制

            // 4. 执行持久化
            stateBackend.snapshot(this.stateStore);

            log.info("Checkpoint finished successfully. Duration: {} ms, State Size: {}",
                    (System.currentTimeMillis() - start), stateStore.size());

        } catch (Exception e) {
            log.error("Checkpoint failed", e);
        } finally {
            // 5. 恢复 Source 写入
            isPausedForCheckpoint.set(false);
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down Risk Engine...");
        isRunning.set(false);

        // 1. 停止接收新数据，等待内部队列处理完
        try {
            // 等待 Dispatcher 线程退出
            Thread.sleep(1000);

            // 2. 强制执行最后一次 Checkpoint
            performCheckpoint();

            // 3. 关闭线程池
            workerPool.shutdown();
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
            scheduler.shutdownNow();

            // 4. 关闭资源
            stateBackend.close();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Risk Engine Shutdown Complete.");
    }
}