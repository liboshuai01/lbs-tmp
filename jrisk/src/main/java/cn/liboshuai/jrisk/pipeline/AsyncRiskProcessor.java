package cn.liboshuai.jrisk.pipeline;

import cn.liboshuai.jrisk.config.ThreadPoolConfig;
import cn.liboshuai.jrisk.core.component.BaseComponent;
import cn.liboshuai.jrisk.core.event.TradeEvent;
import cn.liboshuai.jrisk.model.RiskContext;
import cn.liboshuai.jrisk.model.RiskResult;
import cn.liboshuai.jrisk.model.Rule;
import cn.liboshuai.jrisk.rule.RuleEngine;
import cn.liboshuai.jrisk.rule.RuleManager;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 阶段 2: 核心风控处理器 (Processor)
 * 从缓冲队列消费，执行异步 I/O 加载特征，然后执行规则计算
 */
public class AsyncRiskProcessor extends BaseComponent {

    private final BlockingQueue<TradeEvent> inputQueue;
    private final BlockingQueue<RiskContext> outputQueue;

    // 假设这是下一章要实现的规则引擎接口
    private final RuleManager ruleManager; // 新增
    private final RuleEngine ruleEngine;   // 新增

    private ThreadPoolExecutor ioExecutor;
    private ThreadPoolExecutor computeExecutor;
    private Thread consumerThread;

    public AsyncRiskProcessor(BlockingQueue<TradeEvent> inputQueue,
                              BlockingQueue<RiskContext> outputQueue,
                              RuleManager ruleManager, // 构造注入
                              RuleEngine ruleEngine) {
        super("AsyncRiskProcessor");
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.ruleManager = ruleManager;
        this.ruleEngine = ruleEngine;
    }

    @Override
    protected void doInit() {
        // 获取全局线程池
        this.ioExecutor = ThreadPoolConfig.getIoExecutor();
        this.computeExecutor = ThreadPoolConfig.getComputeExecutor();
    }

    @Override
    protected void doStart() {
        // 启动消费线程，负责从队列取数据并提交给 CompletableFuture
        consumerThread = new Thread(this::runConsumeLoop, "processor-consumer");
        consumerThread.start();
    }

    @Override
    protected void doStop() {
        consumerThread.interrupt();
    }

    private void runConsumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 1. 从队列取出事件 (阻塞等待)
                TradeEvent event = inputQueue.poll(2, TimeUnit.SECONDS);
                if (event == null) continue;

                // 2. 开启异步处理流程 (JUC 核心: CompletableFuture 链式编排)
                processEventAsync(event);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processEventAsync(TradeEvent event) {
        // Step 1: 包装上下文
        RiskContext context = new RiskContext(event);

        // Step 2: 异步加载外部数据 (模拟) -> 运行在 IO 线程池
        CompletableFuture<String> userProfileFuture = CompletableFuture.supplyAsync(() -> {
            // 模拟查 Redis (耗时 10ms)
            sleep(10);
            return "{'level': 'VIP', 'reg_days': 100}";
        }, ioExecutor);

        CompletableFuture<String> historyStatsFuture = CompletableFuture.supplyAsync(() -> {
            // 模拟查 Doris/HBase (耗时 20ms) -> 与上面并行执行
            sleep(20);
            return "{'fail_cnt_5m': 0}";
        }, ioExecutor);

        // Step 3: 数据加载完成后，合并数据 -> 运行在 IO 线程池 (因为是轻量级合并)
        userProfileFuture.thenCombine(historyStatsFuture, (profile, stats) -> {
                    context.addFeature("user_profile", profile);
                    context.addFeature("history_stats", stats);
                    return context;
                })
                // Step 4: 切换到计算线程池执行规则
                .thenApplyAsync(ctx -> {
                    // JUC 读锁：获取当前这一刻的规则快照
                    List<Rule> rules = ruleManager.getActiveRules();

                    for (Rule rule : rules) {
                        // 执行规则
                        RiskResult result = ruleEngine.execute(ctx, rule);
                        ctx.addResult(result);
                    }
                    return ctx;
                }, computeExecutor)
                // Step 5: 处理完成，推入结果队列 -> 这里的线程不确定，取决于上一步谁执行完
                .thenAccept(ctx -> {
                    try {
                        outputQueue.put(ctx);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                // Step 6: 异常兜底
                .exceptionally(ex -> {
                    log.error("Async process failed for event: " + event.getEventId(), ex);
                    return null;
                });
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}