package cn.liboshuai.demo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务基类
 */
@Slf4j
public abstract class StreamTask {
    /**
     * [Legacy 核心] Checkpoint 锁对象
     * 在旧版 Flink 中, Data Stream 和 Control Stream (Checkpoint) 必须争抢这把锁.
     * --- GETTER ---
     * 获取锁对象,供外部或子类使用
     */
    @Getter
    protected final Object checkpointLock = new Object();

    protected volatile boolean isRunning = true;

    /**
     * 启动任务主循环
     */
    public final void invoke() throws Exception {
        log.info("[StreamTask-Legacy] 任务已启动. 使用 CheckpointLock 模型.");
        try {
            runMailLoop();
        } catch (Exception e){
            log.error("[StreamTask-Legacy] 异常: " + e.getMessage());
            throw e;
        } finally {
            close();
        }
    }

    protected abstract void runMailLoop() throws Exception;

    protected void close() {
        log.info("[StreamTask-Legacy] 结束。");
        isRunning = false;
    }

    /**
     * 执行 Checkpoint.
     * 在 Legacy 模型中, 这个方法通常由辅助线程 (如 Timer 线程或 RPC 线程) 直接调用.
     * 而不是像 Mailbox 那样由主线程自己执行.
     */
    public abstract void performCheckpoint(long checkpointId);
}
