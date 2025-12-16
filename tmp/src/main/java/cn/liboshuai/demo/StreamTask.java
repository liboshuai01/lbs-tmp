package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StreamTask {
    protected final Object checkpointLock = new Object();
    protected volatile boolean running = true;

    public void invoke() throws Exception {
        log.info("[StreamTask-Legacy] 任务已启动. 使用 CheckpointLock 模型");
        try {
            runMailLoop();
        } finally {
            close();
        }
    }

    protected abstract void runMailLoop() throws Exception;

    protected void close() {
        log.info("[StreamTask-Legacy] 结束.");
        running = false;
    }

    public abstract void performCheckpoint(long checkpointId);
}
