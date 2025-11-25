package cn.liboshuai.jrisk.pipeline;

import cn.liboshuai.jrisk.core.component.BaseComponent;
import cn.liboshuai.jrisk.model.RiskContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 阶段 3: 结果分发器 (Sink)
 * 消费结果并下发到外部系统
 */
public class SinkDispatcher extends BaseComponent {

    private final BlockingQueue<RiskContext> inputQueue;
    private Thread dispatcherThread;

    public SinkDispatcher(BlockingQueue<RiskContext> inputQueue) {
        super("SinkDispatcher");
        this.inputQueue = inputQueue;
    }

    @Override
    protected void doInit() {}

    @Override
    protected void doStart() {
        dispatcherThread = new Thread(this::runDispatchLoop, "sink-dispatcher");
        dispatcherThread.start();
    }

    @Override
    protected void doStop() {
        dispatcherThread.interrupt();
    }

    private void runDispatchLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                RiskContext result = inputQueue.poll(2, TimeUnit.SECONDS);
                if (result != null) {
                    // 模拟发送到 Kafka
                    if ("REJECT".equals(result.getFinalDecision()) || "REVIEW".equals(result.getFinalDecision())) {
                        log.warn(">>> RISK ALERT [{}]: decision={}, reason={}",
                                result.getEvent().getEventId(),
                                result.getFinalDecision(),
                                result.getResults());
                    } else {
                        // 正常通过的日志可以降级处理
                        log.debug("Pass event: {}", result.getEvent().getEventId());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}