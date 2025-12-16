package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

/**
 * 入口类
 */
@Slf4j
public class EntryPoint {
    public static void main(String[] args) {
        log.info("=== Flink Legacy (Checkpoint Lock) 模型模拟启动 ===");
        MiniInputGate inputGate = new MiniInputGate();

        CounterStreamTask task = new CounterStreamTask(inputGate);
        NettyDataProducer netty = new NettyDataProducer(inputGate);
        CheckpointScheduler checkpoint = new CheckpointScheduler(task);

        netty.start();
        checkpoint.start();
        try {
            task.runMailLoop();
        } catch (Exception e) {
            log.error("崩溃啦: ", e);
        } finally {
            netty.shutdown();
            checkpoint.shutdown();
        }
    }
}
