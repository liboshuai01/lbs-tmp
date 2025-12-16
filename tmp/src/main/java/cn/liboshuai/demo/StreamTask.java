package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StreamTask {
    private final Object checkpointLock = new Object();
    protected boolean running = true;

    public void invoke() {
        log.info("[StreamTask-Legacy] 任务已启动. 使用 CheckpointLock 模型");
        try {
            runMailLoop();
        } catch (Exception e) {
        } finally {

        }
    }

    protected abstract void runMailLoop();

    protected void close() {
        log.info("[StreamTask-Legacy] 结束.");
        running = false;
    }
}
