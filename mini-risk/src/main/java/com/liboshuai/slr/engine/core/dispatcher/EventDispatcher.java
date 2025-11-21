package com.liboshuai.slr.engine.core.dispatcher;

import com.liboshuai.slr.engine.core.engine.RiskEngine;
import com.liboshuai.slr.engine.model.RiskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 分发器
 * 负责将 Kafka 线程拉取到的数据，通过异步提交给计算线程池。
 */
@Slf4j
@Component
public class EventDispatcher {

    @Resource
    @Qualifier("ruleComputeExecutor")
    private ThreadPoolExecutor computeExecutor;

    @Resource
    private RiskEngine riskEngine;

    /**
     * 将事件分发到计算线程
     * 这里利用了 ThreadPoolExecutor 的 BlockingQueue 作为缓冲区
     */
    public void dispatch(RiskEvent event) {
        try {
            // 提交任务。如果队列满了，根据配置的 CallerRunsPolicy，
            // 当前 Kafka 消费者线程会执行该任务，从而阻塞消费，实现"背压"。
            computeExecutor.execute(() -> riskEngine.process(event));
        } catch (Exception e) {
            log.error("Failed to dispatch event: {}", event, e);
        }
    }
}