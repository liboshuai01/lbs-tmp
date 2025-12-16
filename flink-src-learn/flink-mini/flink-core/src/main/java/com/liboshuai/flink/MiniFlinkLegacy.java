package com.liboshuai.flink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MiniFlinkLegacy
 * <p>
 * 模拟 Flink 在引入 Mailbox 模型之前的 "Checkpoint Lock" (锁机制) 模型。
 * <p>
 * 核心逻辑：
 * 1. 存在一个核心对象锁 (checkpointLock)。
 * 2. 主线程 (Task Thread) 在处理每条数据时，必须持有该锁。
 * 3. 触发 Checkpoint 的辅助线程 (Checkpoint Thread) 必须抢占该锁才能执行快照。
 * <p>
 * 劣势体现：
 * - 锁竞争：高吞吐场景下，Checkpoint 线程很难抢到锁，导致 Checkpoint 延迟。
 * - 性能开销：每处理一条(或一批)数据都要频繁加锁/释放锁。
 * - 复杂性：开发者必须时刻警惕，确保在正确的地方加锁，否则会导致状态不一致。
 */
public class MiniFlinkLegacy {

    /**
     * 任务基类 (Legacy Version)
     */
    @Slf4j
    static abstract class StreamTask {

        /**
         * [Legacy 核心] Checkpoint 锁对象
         * 在旧版 Flink 中，Data Stream 和 Control Stream (Checkpoint) 必须争抢这把锁。
         * -- GETTER --
         *  获取锁对象，供外部或子类使用

         */
        @Getter
        protected final Object checkpointLock = new Object();

        protected volatile boolean isRunning = true;

        /**
         * 启动任务主循环
         */
        public final void invoke() throws Exception {
            log.info("[StreamTask-Legacy] 任务已启动。使用 CheckpointLock 模型。");
            try {
                // 具体的处理逻辑交给子类，也就是 mainProcessingLoop
                runMailLoop();
            } catch (Exception e) {
                log.error("[StreamTask-Legacy] 异常：" + e.getMessage());
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
         * 执行 Checkpoint。
         * 在 Legacy 模型中，这个方法通常由辅助线程（如 Timer 线程或 RPC 线程）直接调用，
         * 而不是像 Mailbox 那样由主线程自己执行。
         */
        public abstract void performCheckpoint(long checkpointId);
    }

    /**
     * 简单的阻塞式 InputGate。
     * 旧版模型不需要像 Mailbox 那样复杂的 Future/Yield 机制，
     * 因为主线程大部分时间只需要阻塞在数据读取上，或者阻塞在锁上。
     */
    @Slf4j
    static class MiniInputGate {

        private final Queue<String> queue = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition available = lock.newCondition();

        public void pushData(String data) {
            lock.lock();
            try {
                queue.add(data);
                available.signal(); // 唤醒正在阻塞等待数据的线程
            } finally {
                lock.unlock();
            }
        }

        /**
         * 阻塞式获取数据
         */
        public String pollNextBlocking() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    available.await();
                }
                return queue.poll();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 具体的计数任务实现
     */
    @Slf4j
    static class CounterStreamTask extends StreamTask {

        private final MiniInputGate inputGate;

        // 任务状态：计数器
        // 在 Legacy 模型中，这个状态非常危险，必须严格被 checkpointLock 保护
        private long recordCount = 0;

        public CounterStreamTask(MiniInputGate inputGate) {
            this.inputGate = inputGate;
        }

        /**
         * 主处理循环 (Main Thread)
         */
        @Override
        protected void runMailLoop() throws Exception {
            while (isRunning) {
                // 1. 读取数据 (阻塞式，这里不需要加锁，因为 IO 不涉及状态修改)
                String record = inputGate.pollNextBlocking();

                // 2. 处理数据 [关键区域]
                // 必须加锁！因为 Checkpoint 线程可能随时想来读取 recordCount 做快照。
                // 如果不加锁，可能出现 Checkpoint 读到一半的状态。
                synchronized (checkpointLock) {
                    processRecord(record);
                }
            }
        }

        private void processRecord(String record) {
            this.recordCount++;
            // 模拟一些计算耗时，增加锁占用的时间，加剧竞争
            // 如果这里耗时越长，Checkpoint 线程就越难抢到锁
            try {
                // 简单自旋模拟 CPU 密集型计算，不释放锁
                // Thread.sleep 此时虽然持有锁，但更符合 IO 等待特征，这里用自旋或极短 sleep 模拟计算
                if (recordCount % 100 == 0) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException ignored) {}

            if (recordCount % 10 == 0) {
                log.info("Task (Thread-{}) 处理进度: {} 条", Thread.currentThread().getName(), recordCount);
            }
        }

        /**
         * 执行 Checkpoint (Auxiliary Thread)
         * 注意：这个方法是由 CheckpointScheduler 线程调用的！不是主线程！
         */
        @Override
        public void performCheckpoint(long checkpointId) {
            long startTime = System.currentTimeMillis();

            // [Legacy 痛点]
            // 必须与主线程争抢同一把锁。
            // 如果主线程正在疯狂处理数据（持有锁），这里就会阻塞（Checkpoint 延迟）。
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
    }

    /**
     * 数据生产者 (与 Mailbox 版本一致)
     */
    static class NettyDataProducer extends Thread {
        private final MiniInputGate inputGate;
        private volatile boolean running = true;

        public NettyDataProducer(MiniInputGate inputGate) {
            super("Netty-Thread");
            this.inputGate = inputGate;
        }

        @Override
        public void run() {
            Random random = new Random();
            int seq = 0;
            while (running) {
                try {
                    // 模拟高频数据输入，给锁造成压力
                    int sleep = random.nextInt(100) < 5 ? 200 : 5;
                    TimeUnit.MILLISECONDS.sleep(sleep);

                    String data = "Record-" + (++seq);
                    inputGate.pushData(data);

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

    /**
     * Checkpoint 调度器 (Legacy Version)
     * 核心区别：直接调用 task.performCheckpoint，这会导致跨线程的方法调用，从而触发锁竞争。
     */
    @Slf4j
    static class CheckpointScheduler extends Thread {

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
                    // 在 Legacy 模式下，Scheduler 线程直接调用 Task 的方法。
                    // 这意味着 performCheckpoint 将在这个 Timer 线程中执行，
                    // 并且必须去抢占主线程持有的锁。
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

    /**
     * 入口类
     */
    @Slf4j
    static class EntryPoint {

        public static void main(String[] args) {
            log.info("=== Flink Legacy (Checkpoint Lock) 模型模拟启动 ===");

            MiniInputGate inputGate = new MiniInputGate();
            CounterStreamTask task = new CounterStreamTask(inputGate);

            // 生产者线程
            NettyDataProducer netty = new NettyDataProducer(inputGate);
            netty.start();

            // Checkpoint 定时触发线程
            CheckpointScheduler cpCoordinator = new CheckpointScheduler(task);
            cpCoordinator.start();

            // 主线程启动任务循环
            try {
                task.invoke();
            } catch (Exception e) {
                log.error("Task 崩溃", e);
            } finally {
                netty.shutdown();
                cpCoordinator.shutdown();
            }
        }
    }
}