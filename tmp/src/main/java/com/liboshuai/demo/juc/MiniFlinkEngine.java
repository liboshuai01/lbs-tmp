package com.liboshuai.demo.juc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * MiniFlinkEngine
 * * 一个微型的流计算引擎，旨在演示 Flink 内部如何大量使用 JDK JUC (java.util.concurrent)
 * 来保证高并发下的数据处理、线程安全和任务协调。
 *
 * 包含核心概念：
 * 1. JobMaster (调度器)
 * 2. StreamTask (执行单元)
 * 3. Channel (基于阻塞队列的数据传输，模拟网络栈)
 * 4. CheckpointLock (源于 Flink 的 Source 锁机制)
 */
public class MiniFlinkEngine {

    // ==========================================
    // 1. 基础数据结构与接口
    // ==========================================

    /**
     * 模拟 Flink 的 StreamRecord，数据流动的基本单元
     */
    static class StreamRecord<T> {
        final T value;
        final long timestamp;

        public StreamRecord(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "Record{" + "val=" + value + ", ts=" + timestamp + '}';
        }
    }

    /**
     * 简化的算子接口
     */
    interface Operator<IN, OUT> {
        void processElement(StreamRecord<IN> element, Collector<OUT> ctx) throws Exception;
    }

    interface Collector<T> {
        void collect(T record);
    }

    interface SourceFunction<T> {
        void run(SourceContext<T> ctx) throws Exception;
        void cancel();
    }

    interface SourceContext<T> {
        void collect(T element);
        Object getCheckpointLock(); // Flink 核心机制：Source 锁
    }

    interface SinkFunction<T> {
        void invoke(T value);
    }

    // ==========================================
    // 2. 核心组件：数据通道 (Channel)
    // ==========================================

    /**
     * 模拟 Flink 的 Network Stack 中的 InputChannel/ResultPartition。
     * * JUC 知识点: BlockingQueue
     * 用途:
     * 1. 线程安全的数据交换（生产者-消费者模型）。
     * 2. 模拟 "背压" (Backpressure)：当队列满时，上游 Task 的 put 操作会阻塞，
     * 自然降低了上游的生产速度，防止 OOM。
     */
    static class DataChannel<T> {
        // 使用 LinkedBlockingQueue 实现有界队列
        private final BlockingQueue<StreamRecord<T>> queue;
        private volatile boolean isClosed = false; // volatile 保证可见性

        public DataChannel(int capacity) {
            this.queue = new LinkedBlockingQueue<>(capacity);
        }

        public void push(StreamRecord<T> record) throws InterruptedException {
            if (isClosed) return;
            // put 是阻塞操作，队列满则等待 -> 产生背压
            queue.put(record);
        }

        public StreamRecord<T> pull() throws InterruptedException {
            // take 是阻塞操作，队列空则等待
            return queue.take();
        }

        public void close() {
            this.isClosed = true;
        }

        public boolean isClosed() {
            return isClosed && queue.isEmpty();
        }
    }

    // ==========================================
    // 3. 核心组件：StreamTask (执行线程)
    // ==========================================

    /**
     * 模拟 Flink 的 StreamTask。每个 Task 运行在一个独立的线程中。
     * * JUC 知识点:
     * 1. Runnable (任务定义)
     * 2. AtomicInteger (Metrics 统计)
     * 3. volatile (状态标志)
     * 4. ReentrantLock (锁机制)
     */
    static abstract class StreamTask implements Runnable {
        protected final String taskName;
        // 模拟 Flink 的 Metrics，使用 Atomic 保证高并发下的计数准确且高性能
        protected final AtomicInteger processedCount = new AtomicInteger(0);
        // 任务运行状态，volatile 保证多线程间的可见性（主线程 cancel，工作线程立马能看到）
        protected volatile boolean isRunning = true;

        // 模拟 Flink 的 Checkpoint Lock 或 全局任务锁
        // 用于保护非线程安全的状态访问，或在 Source 中保证发射数据与 Checkpoint 的原子性
        protected final ReentrantLock lock = new ReentrantLock();

        public StreamTask(String name) {
            this.taskName = name;
        }

        public void cancel() {
            this.isRunning = false;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        @Override
        public String toString() {
            return taskName;
        }
    }

    /**
     * SourceTask: 数据源任务
     */
    static class SourceTask<T> extends StreamTask {
        private final SourceFunction<T> sourceFunction;
        private final DataChannel<T> outputChannel;

        public SourceTask(String name, SourceFunction<T> sourceFunction, DataChannel<T> outputChannel) {
            super(name);
            this.sourceFunction = sourceFunction;
            this.outputChannel = outputChannel;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " started.");
            try {
                // 构造 SourceContext
                SourceContext<T> ctx = new SourceContext<T>() {
                    @Override
                    public void collect(T element) {
                        // JUC 知识点: ReentrantLock
                        // 在 Flink 中，Source 发送数据时通常需要持有锁，以防止与 Checkpoint 线程冲突
                        // 这里模拟了这个关键行为。
                        synchronized (getCheckpointLock()) {
                            try {
                                outputChannel.push(new StreamRecord<>(element));
                                processedCount.incrementAndGet();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }

                    @Override
                    public Object getCheckpointLock() {
                        // 返回 Task 级别的锁对象
                        return SourceTask.this.lock;
                    }
                };

                sourceFunction.run(ctx);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 标记通道关闭，通知下游
                outputChannel.close();
                System.out.println(taskName + " finished. Total emitted: " + processedCount.get());
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            sourceFunction.cancel();
        }
    }

    /**
     * ProcessingTask: 处理任务 (Map/Filter/Process)
     */
    static class OneInputStreamTask<IN, OUT> extends StreamTask {
        private final DataChannel<IN> inputChannel;
        private final DataChannel<OUT> outputChannel;
        private final Operator<IN, OUT> operator;

        public OneInputStreamTask(String name,
                                  DataChannel<IN> input,
                                  DataChannel<OUT> output,
                                  Operator<IN, OUT> op) {
            super(name);
            this.inputChannel = input;
            this.outputChannel = output;
            this.operator = op;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " started.");
            try {
                Collector<OUT> collector = (record) -> {
                    try {
                        if (outputChannel != null) {
                            outputChannel.push(new StreamRecord<>(record));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                };

                while (isRunning && !inputChannel.isClosed()) {
                    // 阻塞获取上游数据
                    StreamRecord<IN> record = inputChannel.pull();
                    if (record != null) {
                        // JUC 知识点: 细粒度锁 (可选)
                        // Flink 实际处理中会保证单线程处理 record，但在 StateBackend 访问时可能涉及锁
                        lock.lock();
                        try {
                            operator.processElement(record, collector);
                            processedCount.incrementAndGet();
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                // 响应中断
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputChannel != null) outputChannel.close();
                System.out.println(taskName + " finished. Total processed: " + processedCount.get());
            }
        }
    }

    // ==========================================
    // 4. 调度与执行环境: JobMaster
    // ==========================================

    /**
     * 模拟 Flink 的 JobMaster / Dispatcher。
     * * JUC 知识点:
     * 1. ThreadPoolExecutor (自定义线程池)
     * 2. ThreadFactory (自定义线程命名)
     * 3. CompletableFuture (异步任务结果)
     * 4. CountDownLatch (协调任务启动)
     */
    public static class JobEnvironment {

        // 模拟 Flink 的 Slot 管理，使用线程池
        private final ExecutorService taskExecutor;
        private final List<StreamTask> tasks = new ArrayList<>();

        public JobEnvironment(int parallelism) {
            // JUC 知识点: ThreadFactory
            // Flink 中所有线程都有规范的命名 (例如: "Source Filter -> Sink (1/4)")
            // 这里我们模拟这一行为，这对于多线程 Debug 非常重要。
            ThreadFactory flinkThreadFactory = new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "MiniFlink-Task-" + count.getAndIncrement());
                }
            };

            // JUC 知识点: ThreadPoolExecutor
            // 直接使用 ExecutorService 的实现类，手动控制参数
            this.taskExecutor = new ThreadPoolExecutor(
                    parallelism, parallelism, // core, max
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    flinkThreadFactory
            );
        }

        /**
         * 提交并执行作业
         * JUC 知识点: CompletableFuture
         * 现代异步编程标准。Flink 的 JobClient.submitJob() 返回的就是 CompletableFuture。
         */
        public CompletableFuture<Void> executeAsync() {
            // JUC 知识点: CountDownLatch
            // 这是一个"倒计时门闩"。
            // 用途: 确保所有 Task 线程都已经提交到线程池并且准备就绪后，主线程才继续逻辑（模拟 Flink 的部署阶段）。
            CountDownLatch allTasksReadyLatch = new CountDownLatch(tasks.size());

            CompletableFuture<Void> jobResult = new CompletableFuture<>();

            System.out.println(">>> Job Starting... Deploying " + tasks.size() + " tasks.");

            for (StreamTask task : tasks) {
                taskExecutor.submit(() -> {
                    // 倒计时减一
                    allTasksReadyLatch.countDown();
                    task.run();
                });
            }

            try {
                // 等待所有任务都已开始调度
                allTasksReadyLatch.await();
                System.out.println(">>> All tasks are running.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 简单的监控线程，模拟 JobMaster 监控 Job 状态
            new Thread(() -> {
                try {
                    // 简单等待足够长的时间演示运行效果，或者等待任务结束逻辑
                    // 实际 Flink 会监听 Task 的状态更新
                    Thread.sleep(100);
                    jobResult.complete(null);
                } catch (Exception e) {
                    jobResult.completeExceptionally(e);
                }
            }, "MiniFlink-JobMaster").start();

            return jobResult;
        }

        public void stop() {
            // 取消所有任务
            for (StreamTask task : tasks) {
                task.cancel();
            }
            taskExecutor.shutdownNow();
        }

        // ------- 构建拓扑的方法 (Chain) -------

        public <OUT> DataChannel<OUT> addSource(SourceFunction<OUT> sourceFunc, String name) {
            DataChannel<OUT> channel = new DataChannel<>(100); // 缓冲大小 100
            tasks.add(new SourceTask<>(name, sourceFunc, channel));
            return channel;
        }

        public <IN, OUT> DataChannel<OUT> addMap(DataChannel<IN> input, Operator<IN, OUT> op, String name) {
            DataChannel<OUT> channel = new DataChannel<>(100);
            tasks.add(new OneInputStreamTask<>(name, input, channel, op));
            return channel;
        }

        public <IN> void addSink(DataChannel<IN> input, SinkFunction<IN> sink, String name) {
            // Sink 也是一种 OneInputStreamTask，只是没有 OutputChannel
            tasks.add(new OneInputStreamTask<>(name, input, null, (record, ctx) -> {
                sink.invoke(record.value);
            }));
        }
    }

    // ==========================================
    // 5. 用户代码示例 (Main)
    // ==========================================

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 初始化环境，并行度 4 (线程池大小)
        JobEnvironment env = new JobEnvironment(4);

        // 2. 定义 Source (模拟 Kafka 消费)
        // 这是一个典型的多线程场景：Source 线程生产，Map 线程消费
        SourceFunction<String> mySource = new SourceFunction<String>() {
            private volatile boolean isRunning = true;

            @Override
            public void run(SourceContext<String> ctx) throws Exception {
                int i = 0;
                while (isRunning && i < 50) {
                    // 模拟持有锁保护状态
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect("Event-" + i);
                    }
                    i++;
                    Thread.sleep(10); // 模拟生产延迟
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        };

        // 3. 构建 DAG (Source -> Map -> Sink)
        // Channel 充当了 Flink 中的 Edge，连接两个 Vertex (Task)
        DataChannel<String> sourceOut = env.addSource(mySource, "Source-Thread");

        DataChannel<String> mapOut = env.addMap(sourceOut, (record, out) -> {
            String upper = record.value.toUpperCase();
            // 可以在这里加上 synchronized (lock) 模拟 Flink 的 State 访问
            out.collect(upper);
        }, "Map-Thread");

        env.addSink(mapOut, (value) -> {
            System.out.println("Sink Received: " + value);
        }, "Sink-Thread");

        // 4. 异步提交作业
        CompletableFuture<Void> result = env.executeAsync();

        // 5. 主线程可以做其他事，这里我们阻塞等待作业完成 (或者等待一段时间后取消)
        try {
            // 模拟运行 2 秒后停止
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(">>> Stopping Job...");
            env.stop();
        }
    }
}
