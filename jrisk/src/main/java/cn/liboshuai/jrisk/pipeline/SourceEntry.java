package cn.liboshuai.jrisk.pipeline;

import cn.liboshuai.jrisk.core.component.BaseComponent;
import cn.liboshuai.jrisk.core.event.TradeEvent;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 阶段 1: 交易接入网关 (Source)
 * 模拟从 Kafka 拉取数据并推入缓冲队列
 */
public class SourceEntry extends BaseComponent {

    private final BlockingQueue<TradeEvent> outputQueue;
    private Thread sourceThread;

    public SourceEntry(BlockingQueue<TradeEvent> outputQueue) {
        super("SourceEntry");
        this.outputQueue = outputQueue;
    }

    @Override
    protected void doInit() {
        // 初始化 Kafka 客户端等资源
    }

    @Override
    protected void doStart() {
        // 启动一个单独的线程来模拟数据接入
        sourceThread = new Thread(this::runSourceLoop, "source-thread");
        sourceThread.start();
    }

    @Override
    protected void doStop() {
        // 中断拉取线程
        if (sourceThread != null) {
            sourceThread.interrupt();
        }
    }

    private void runSourceLoop() {
        Random random = new Random();
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 1. 模拟接收交易数据
                TradeEvent event = new TradeEvent();
                event.setUserId("user_" + random.nextInt(100));
                event.setOrderId("order_" + System.nanoTime());
                event.setAmount(BigDecimal.valueOf(random.nextDouble() * 1000));
                event.setTradeType("PAYMENT");
                event.setIp("192.168.1." + random.nextInt(255));

                // 2. 推入队列 (JUC 关键点: 队列满时阻塞，实现反压)
                // 使用 offer 带超时，避免在停止时死锁
                if (!outputQueue.offer(event, 2, TimeUnit.SECONDS)) {
                    log.warn("Buffer queue is full! Backpressure triggered.");
                }

                // 模拟流量间隔 (1ms ~ 5ms)
                Thread.sleep(random.nextInt(5) + 1);

            } catch (InterruptedException e) {
                log.info("Source thread interrupted, exiting...");
                Thread.currentThread().interrupt(); // 恢复中断状态
                break;
            } catch (Exception e) {
                log.error("Error processing source event", e);
            }
        }
    }
}