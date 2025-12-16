package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CounterStreamTask extends StreamTask {

    private final MiniInputGate miniInputGate;

    private long recordCount = 0;

    public CounterStreamTask(MiniInputGate miniInputGate) {
        this.miniInputGate = miniInputGate;
    }

    @Override
    protected void runMailLoop() throws Exception {
        while (running) {
            String record = miniInputGate.pollNext();
            synchronized (checkpointLock) {
                processRecord(record);
            }
        }
    }

    @Override
    public void performCheckpoint(long checkpointId) {
        long startTime = System.currentTimeMillis();
        synchronized (checkpointLock) {
            long waitTime = System.currentTimeMillis() - startTime;
            if (waitTime > 5) {
                log.warn("[Checkpoint] 抢锁耗时 {} ms (主线程太忙了！)", waitTime);
            }
            log.info(" >>> [Checkpoint Starting] ID: {}, 捕获状态值: {} (Thread-{})",
                    checkpointId, recordCount, Thread.currentThread().getName());
            // 模拟快照耗时 (同步阶段)
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info(" <<< [Checkpoint Finished] ID: {} 完成", checkpointId);
        }
    }

    private void processRecord(String ignore) {
        this.recordCount++;
        try {
            if (recordCount % 100 == 0) {
                TimeUnit.MILLISECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (recordCount % 10 == 0) {
            log.info("Task (Thread-{}) 处理进度: {} 条", Thread.currentThread().getName(), recordCount);
        }
    }
}
