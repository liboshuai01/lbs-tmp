package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Checkpoint 调度器 (Legacy Version)
 * 核心区别: 直接调用 task.performCheckpoint, 这会导致跨线程的方法调用, 从而触发锁竞争.
 */
@Slf4j
public class CheckpointScheduler extends Thread{
    private final CounterStreamTask task;
    private volatile boolean running = true;

    public CheckpointScheduler(CounterStreamTask task) {
        super("Checkpoint-Timer");
        this.task = task;
    }

    @Override
    public void run() {
        long checkpointId = 0;
        while (running) {
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
                long id = ++checkpointId;
                log.info("[JM] 触发 Checkpoint {}", id);
                // === 关键区别 ===
                // 在 Legacy 模式下, Scheduler 线程直接调用 Task 的方法.
                // 这意味着 performCheckpoint 将在这个 Timer 线程中执行.
                // 并且必须去抢占主线持有的锁.
                task.performCheckpoint(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
