package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CheckpointScheduler extends Thread {

    public final StreamTask task;
    private volatile boolean running = true;

    public CheckpointScheduler(StreamTask task) {
        super("Checkpoint-Thread");
        this.task = task;
    }

    @Override
    public void run() {
        long checkpointId = 0;
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long id = checkpointId++;
            log.info("[JM] 触发 Checkpoint {}", id);
            task.performCheckpoint(id);
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
