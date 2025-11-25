package cn.liboshuai.jrisk;

import cn.liboshuai.jrisk.core.event.TradeEvent;
import cn.liboshuai.jrisk.core.queue.BoundedQueueFactory;
import cn.liboshuai.jrisk.model.RiskContext;
import cn.liboshuai.jrisk.pipeline.AsyncRiskProcessor;
import cn.liboshuai.jrisk.pipeline.SinkDispatcher;
import cn.liboshuai.jrisk.pipeline.SourceEntry;
import cn.liboshuai.jrisk.rule.RuleEngine;
import cn.liboshuai.jrisk.rule.RuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * 项目启动类
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Starting JRisk System (JDK 1.8 + JUC)...");

        // 1. 创建反压队列 (核心骨架)
        // 接入缓冲: 1000 个，满了就阻塞 Source
        BlockingQueue<TradeEvent> sourceQueue = BoundedQueueFactory.createQueue(1000);
        // 结果缓冲: 1000 个
        BlockingQueue<RiskContext> sinkQueue = BoundedQueueFactory.createQueue(1000);

        // 2. 初始化功能组件
        RuleManager ruleManager = new RuleManager();
        RuleEngine ruleEngine = new RuleEngine();

        SourceEntry source = new SourceEntry(sourceQueue);
        AsyncRiskProcessor processor = new AsyncRiskProcessor(sourceQueue, sinkQueue, ruleManager, ruleEngine);
        SinkDispatcher sink = new SinkDispatcher(sinkQueue);

        // 3. 按顺序初始化
        ruleManager.init();
        source.init();
        processor.init();
        sink.init();

        // 4. 启动组件 (倒序启动，先启动消费者，再启动生产者，防止积压)
        ruleManager.start();
        sink.start();
        processor.start();
        source.start();

        // 5. 注册优雅停机 Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered...");
            source.stop();     // 先停入口
            processor.stop();  // 处理完剩余数据
            sink.stop();       // 发送完结果
            ruleManager.stop();
            log.info("System shutdown complete.");
        }));

        // 6. 阻塞主线程，保持进程运行
        log.info("System acts as a daemon. Press Ctrl+C to exit.");
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            log.warn("Main thread interrupted.");
        }
    }
}