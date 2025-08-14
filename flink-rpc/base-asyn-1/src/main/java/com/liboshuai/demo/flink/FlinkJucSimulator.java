package com.liboshuai.demo.flink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Flink 核心工作流与 JUC 结合的模拟实例
 *
 * @author Gemini AI Assistant
 * @version 1.18 (Simulated)
 */
public class FlinkJucSimulator {

    // ===================================================================================
    // 1. 数据模型 (Data Models)
    // ===================================================================================

    /**
     * 代表一个计算作业 (Job)
     */
    static class Job {
        private final String jobName;
        private final int taskCount;

        public Job(String jobName, int taskCount) {
            this.jobName = jobName;
            this.taskCount = taskCount;
        }

        public String getJobName() { return jobName; }
        public int getTaskCount() { return taskCount; }
    }

    /**
     * 代表一个可执行的任务 (Task)，它是一个 Callable，可以返回结果。
     * 实际 Flink 中的 Task 要复杂得多，这里简化为模拟耗时操作。
     */
    static class Task implements Callable<String> {
        private final int taskId;
        private final String jobName;
        // JUC - CompletableFuture: 持有对外部Future的引用，当任务完成时，通过它通知JobManager
        private final CompletableFuture<String> resultFuture;

        public Task(int taskId, String jobName, CompletableFuture<String> resultFuture) {
            this.taskId = taskId;
            this.jobName = jobName;
            this.resultFuture = resultFuture;
        }

        @Override
        public String call() throws Exception {
            String threadName = Thread.currentThread().getName();
            System.out.printf(">>> [Task-%d] of Job '%s' is starting on thread: %s\n", taskId, jobName, threadName);
            // 模拟真实工作负载
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
            String result = String.format("Result of Task-%d", taskId);

            // JUC - CompletableFuture: 任务执行完毕，调用complete方法，触发后续的异步处理
            resultFuture.complete(result);

            System.out.printf("<<< [Task-%d] of Job '%s' finished. Result: '%s'\n", taskId, jobName, result);
            return result;
        }

        public int getTaskId() {
            return taskId;
        }
    }


    // ===================================================================================
    // 2. TaskManager (Worker Node)
    // ===================================================================================

    static class TaskManager {
        private final String tmId;

        // JUC - AtomicInteger: 无锁化地管理可用槽位数量，高并发下性能优于Lock
        private final AtomicInteger availableSlots;

        // JUC - ExecutorService: 自定义线程池，是TaskManager执行Task的核心。
        // Flink 中 TM 的资源管理就是基于此。我们使用自定义ThreadFactory来命名线程，便于调试。
        private final ExecutorService taskExecutor;

        // JUC - BlockingQueue: 任务接收队列。JobManager向该队列提交任务（生产者），
        // TM的内部线程池消费任务（消费者），实现了解耦和削峰填谷。
        private final BlockingQueue<Task> taskQueue;

        private final Thread dispatcherThread; // 内部调度线程，模拟TM从队列取任务并执行
        private volatile boolean isRunning = true;


        public TaskManager(String tmId, int totalSlots) {
            this.tmId = tmId;
            this.availableSlots = new AtomicInteger(totalSlots);

            // JUC - ThreadFactory: 自定义线程工厂，给线程起个有意义的名字，方便问题排查
            ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicInteger threadCounter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("%s-thread-%d", tmId, threadCounter.getAndIncrement()));
                }
            };

            this.taskExecutor = new ThreadPoolExecutor(
                    totalSlots, totalSlots,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    threadFactory);

            this.taskQueue = new LinkedBlockingQueue<>();

            // 启动一个分发线程，不断从taskQueue中取出任务，并交给线程池执行
            this.dispatcherThread = new Thread(this::dispatch);
            this.dispatcherThread.setName(tmId + "-dispatcher");
        }

        public void start() {
            this.dispatcherThread.start();
            System.out.printf("[TM-%s] Started with %d slots.\n", tmId, availableSlots.get());
        }

        private void dispatch() {
            while(isRunning) {
                try {
                    // JUC - BlockingQueue: take()方法会阻塞，直到队列中有可用的Task，非常高效
                    Task task = taskQueue.take();
                    System.out.printf("[TM-%s] Dispatched Task-%d to executor.\n", tmId, task.getTaskId());
                    availableSlots.decrementAndGet();

                    // 提交给线程池执行，并监听完成事件
                    CompletableFuture.runAsync(() -> {
                        try {
                            task.call();
                        } catch (Exception e) {
                            task.resultFuture.completeExceptionally(e);
                        } finally {
                            // JUC - AtomicInteger: 任务执行完毕（无论成功或失败），归还一个slot
                            availableSlots.incrementAndGet();
                        }
                    }, taskExecutor);

                } catch (InterruptedException e) {
                    if (!isRunning) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // JobManager调用的入口，用于提交任务
        public boolean submitTask(Task task) {
            // 模拟TM有自己的判断逻辑，比如是否超载等
            if (taskQueue.offer(task)) { // offer是非阻塞的
                System.out.printf("[JM -> TM-%s] Task-%d submitted successfully to queue.\n", tmId, task.getTaskId());
                return true;
            } else {
                System.out.printf("[JM -> TM-%s] Task-%d submission failed, queue is full.\n", tmId, task.getTaskId());
                return false;
            }
        }

        public void shutdown() {
            this.isRunning = false;
            this.dispatcherThread.interrupt();
            this.taskExecutor.shutdown();
            try {
                if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    taskExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                taskExecutor.shutdownNow();
            }
            System.out.printf("[TM-%s] has been shut down.\n", tmId);
        }

        public String getTmId() { return tmId; }
        public int getAvailableSlots() { return availableSlots.get(); }
    }


    // ===================================================================================
    // 3. JobManager (Master Node)
    // ===================================================================================

    static class JobManager {
        // JUC - ConcurrentHashMap: 线程安全地存储和管理所有注册的TaskManager
        // Key: TaskManager ID, Value: TaskManager instance
        private final ConcurrentHashMap<String, TaskManager> registeredTaskManagers = new ConcurrentHashMap<>();

        // JUC - ConcurrentHashMap: 线程安全地跟踪每个Job的所有Task的Future
        // Key: Job Name, Value: List of Futures for all tasks in that job
        private final ConcurrentHashMap<String, List<CompletableFuture<String>>> jobFutures = new ConcurrentHashMap<>();

        // JUC - AtomicInteger: 无锁化生成全局唯一的Task ID
        private final AtomicInteger taskIdGenerator = new AtomicInteger(0);

        // JUC - ReentrantLock: 用于保护复杂操作的原子性，例如任务分发策略可能很复杂，
        // 需要查找最空闲的TM，这个过程需要加锁保护，防止多个Job提交时产生竞态条件。
        private final ReentrantLock dispatchLock = new ReentrantLock();

        private int roundRobinCounter = 0;

        public void registerTaskManager(TaskManager tm) {
            registeredTaskManagers.put(tm.getTmId(), tm);
            System.out.printf("[JM] TaskManager %s registered.\n", tm.getTmId());
        }

        /**
         * 提交一个Job，并返回一个代表整个Job完成的CompletableFuture
         */
        public CompletableFuture<Void> submitJob(Job job) {
            System.out.printf("[JM] Received new Job: '%s' with %d tasks.\n", job.getJobName(), job.getTaskCount());

            List<CompletableFuture<String>> taskFutures = new CopyOnWriteArrayList<>();
            jobFutures.put(job.getJobName(), taskFutures);

            List<Task> tasksToDispatch = new ArrayList<>();

            // 1. 创建所有Task实例
            for (int i = 0; i < job.getTaskCount(); i++) {
                int taskId = taskIdGenerator.incrementAndGet();
                CompletableFuture<String> future = new CompletableFuture<>();
                taskFutures.add(future);
                tasksToDispatch.add(new Task(taskId, job.getJobName(), future));
            }

            // 2. 分发所有Task
            dispatchTasks(tasksToDispatch);

            // 3. JUC - CompletableFuture.allOf:
            // 聚合所有Task的Future。当所有Task都完成时，这个聚合的Future才会完成。
            // 这是实现异步流程控制和等待的关键。
            CompletableFuture<Void> allTasksFuture = CompletableFuture.allOf(taskFutures.toArray(new CompletableFuture[0]));

            return allTasksFuture.whenComplete((v, ex) -> {
                if (ex == null) {
                    System.out.printf("[JM] Job '%s' completed successfully!\n", job.getJobName());
                    // 可以在这里收集并打印所有结果
                    // taskFutures.forEach(f -> { try { System.out.println(f.get()); } catch (Exception e){}});
                } else {
                    System.out.printf("[JM] Job '%s' failed: %s\n", job.getJobName(), ex.getMessage());
                }
                jobFutures.remove(job.getJobName());
            });
        }

        private void dispatchTasks(List<Task> tasks) {
            dispatchLock.lock(); // 加锁确保分发逻辑的原子性
            try {
                if (registeredTaskManagers.isEmpty()) {
                    System.err.println("[JM] No TaskManagers registered. Cannot dispatch tasks.");
                    // 让所有future异常完成
                    tasks.forEach(task -> task.resultFuture.completeExceptionally(new RuntimeException("No TaskManagers available.")));
                    return;
                }

                List<TaskManager> tmList = new ArrayList<>(registeredTaskManagers.values());

                for(Task task : tasks) {
                    // 使用简单的轮询策略(Round-Robin)来分发任务
                    TaskManager targetTm = tmList.get(roundRobinCounter % tmList.size());
                    System.out.printf("[JM] Dispatching Task-%d to TM-%s...\n", task.getTaskId(), targetTm.getTmId());
                    targetTm.submitTask(task);
                    roundRobinCounter++;
                }
            } finally {
                dispatchLock.unlock(); // 确保锁被释放
            }
        }

        public void shutdownAll() {
            System.out.println("[JM] Shutting down all registered TaskManagers...");
            registeredTaskManagers.values().forEach(TaskManager::shutdown);
            System.out.println("[JM] Shutdown complete.");
        }
    }


    // ===================================================================================
    // 4. 主程序入口 (Main)
    // ===================================================================================
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("--- Flink JUC Simulation Starting ---");

        // 1. 初始化JobManager
        JobManager jobManager = new JobManager();

        // 2. 初始化并启动2个TaskManager
        TaskManager tm1 = new TaskManager("TM-1", 4); // 4个槽位
        TaskManager tm2 = new TaskManager("TM-2", 4); // 4个槽位

        tm1.start();
        tm2.start();

        // 3. 向JobManager注册TaskManager
        jobManager.registerTaskManager(tm1);
        jobManager.registerTaskManager(tm2);

        // JUC - CountDownLatch (这里用Thread.sleep简化，但实际场景中可用Latch)
        // 确保TaskManager已经完全启动并准备就绪
        Thread.sleep(1000);

        // 4. 创建并提交一个Job
        Job streamProcessingJob = new Job("Real-time-ETL", 10); // Job有10个并行任务
        CompletableFuture<Void> jobCompletionFuture = jobManager.submitJob(streamProcessingJob);

        System.out.println("[Main] Job has been submitted. Main thread is now free to do other things or wait.");

        // 5. 异步等待Job完成
        // main线程可以不阻塞，这里为了演示，我们调用join()来等待最终结果
        jobCompletionFuture.join();

        System.out.println("[Main] Job completion confirmed by main thread.");

        // 6. 关闭系统
        jobManager.shutdownAll();

        System.out.println("--- Flink JUC Simulation Finished ---");
    }
}