package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CounterStreamTask extends StreamTask {

    private final MiniInputGate inputGate;

    // 任务状态: 计数器
    // 在 Legacy 模型中, 这个状态非常危险, 必须严格被 checkpointLock 保护.
    private long recordCount = 0;

    public CounterStreamTask(MiniInputGate inputGate) {
        this.inputGate = inputGate;
    }

    @Override
    protected void runMailLoop() throws Exception {
        while (isRunning) {
            // 1. 读取数据 (阻塞式, 这里不需要加锁, 因为 IO 不涉及状态修改)
            String record = inputGate.pullNextBlocking();
            // 2. 处理数据 [关键区域]
            // 必须加锁! 因为 Checkpoint 线程可能随时想来读取 recordCount 来做快照.
            // 如果不加锁, 可能出现 Checkpoint 读到一半的状态.
            synchronized (checkpointLock) {
                processRecord(record);
            }
        }
    }

    private void processRecord(String record) {
        this.recordCount++;
        // 模拟一些计算耗时, 增加锁占用的时间, 加剧竞争.
        // 如果这里耗时越长, Checkpoint 线程就越难抢到锁.
        try {
            // 简单自选模拟 CPU 密集型计算, 不释放锁
            // Thread.sleep 此时虽然持有锁, 但更符合 IO 等待特征, 这里用自选或极短 sleep 模拟计算.
            if (recordCount % 100 == 0) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {Thread.currentThread().interrupt();}
        if (recordCount % 10 == 0) {
            log.info("Task (Thread-{}) 处理进度: {} 条", Thread.currentThread().getName(), recordCount);
        }
    }

    /**
     * 执行 Checkpoint (Auxiliary Thread)
     * 注意: 这个方法由 CheckpointScheduler 线程调用的! 不是主线程!
     */
    @Override
    public void performCheckpoint(long checkpointId) {
        long startTime = System.currentTimeMillis();

        // [Legacy 痛点]
        // 必须与主线程争抢同一把锁
        // 如果主线程正在疯狂处理数据 (持有锁), 这里就会阻塞 (Checkpoint 延迟).
        synchronized (checkpointLock) {
            long waitTime = System.currentTimeMillis() - startTime;
            if (waitTime > 5) {
                log.warn("[Checkpoint] 枪锁耗时 {} ms (主线程太忙了!)", waitTime);
            }
            log.info(" >>> [Checkpoint Starting] ID: {}, 捕获状态值: {} (Thread-{})", checkpointId, recordCount, Thread.currentThread().getName());
            // 模拟快照耗时 (同步阶段)
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info(" <<< [Checkpoint Finished] ID: {} 完成", checkpointId);
        }
    }
}
