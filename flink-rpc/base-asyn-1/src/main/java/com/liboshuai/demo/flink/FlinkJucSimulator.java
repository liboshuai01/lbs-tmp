package com.liboshuai.demo.flink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FlinkJucSimulator {

    // ===================================================================================
    // 1. 数据模型与枚举 (Data Models & Enums)
    // ===================================================================================

    enum TaskStatus { PENDING, DEPLOYING, RUNNING, FINISHED, FAILED }
    enum JobStatus { CREATED, RUNNING, FINISHED, FAILED }

    /**
     * 代表一个任务，是最小的执行单元
     */
    static class Task {
        final String taskId;
        final String jobName;
        AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.PENDING);

        public Task(String jobId, int taskIndex) {
            this.jobName = jobId;
            this.taskId = "task_" + taskIndex;
        }

        public String getTaskId() { return taskId; }

        public void setStatus(TaskStatus status) {
            this.status.set(status);
            System.out.printf("[Task] '%s' of job '%s' status changed to %s on thread %s%n",
                    taskId, jobName, status, Thread.currentThread().getName());
        }

        // 模拟任务执行，可能会成功也可能会失败
        public String execute() throws Exception {
            setStatus(TaskStatus.RUNNING);
            // 模拟耗时工作
            Thread.sleep(1000 + new Random().nextInt(1000));
            if (new Random().nextInt(10) < 2) { // 20%的概率失败
                setStatus(TaskStatus.FAILED);
                throw new RuntimeException("Task " + taskId + " failed due to a random error.");
            }
            setStatus(TaskStatus.FINISHED);
            return "Task " + taskId + " executed successfully.";
        }
    }

    /**
     * 代表一个作业，由多个任务构成
     */
    static class Job {
        final String jobName;
        final List<Task> tasks;

        public Job(String jobName, int taskCount) {
            this.jobName = jobName;
            this.tasks = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                tasks.add(new Task(jobName, i));
            }
        }
    }


    // ===================================================================================
    // 2. TaskManager: 任务执行者 (Worker Node)
    // ===================================================================================

    static class TaskManager {
        private final String tmId;
        private final JobManager jobManager;
        /**
         * 【JUC应用】自定义线程池 (ExecutorService):
         * 模拟 TaskManager 的核心计算资源。每个 TaskManager 有一个固定大小的线程池，
         * 其大小代表了它有多少个 "Task Slot"。收到的任务会提交到这个池中并发执行。
         */
        private final ExecutorService taskSlotPool;

        /**
         * 【JUC应用】信号量 (Semaphore):
         * 这是对 Task Slot 概念更精确的模拟。即使线程池很大，我们也可以通过 Semaphore
         * 来限制同时运行的任务数量，精确控制并发度。这里的大小和线程池大小一致。
         */
        private final Semaphore taskSlots;

        public TaskManager(String tmId, int numberOfSlots, JobManager jobManager) {
            this.tmId = tmId;
            this.jobManager = jobManager;
            this.taskSlots = new Semaphore(numberOfSlots);
            // 使用自定义的线程工厂，方便调试，可以看清是哪个TaskManager的线程在工作
            this.taskSlotPool = Executors.newFixedThreadPool(numberOfSlots,
                    r -> new Thread(r, "tm-" + tmId + "-slot-thread"));
        }

        public void register() {
            System.out.printf("[TM] TaskManager %s is registering with JobManager.%n", tmId);
            jobManager.registerTaskManager(tmId, this);
        }

        /**
         * TaskManager 接收并执行一个任务
         * @return CompletableFuture<String> 异步返回任务执行结果
         */
        public CompletableFuture<String> executeTask(Task task) {
            /**
             * 【JUC应用】现代异步处理 (CompletableFuture):
             * 这是现代Java异步编程的核心。TaskManager 接收任务后，不是阻塞等待其完成，
             * 而是立即返回一个 CompletableFuture。这使得 JobManager 可以非阻塞地分发大量任务。
             * 任务的实际执行、成功或失败，都会在这个 Future 中得到体现。
             */
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // 1. 获取一个 "Slot"
                    taskSlots.acquire();
                    System.out.printf("[TM-%s] Slot acquired for task %s. Available slots: %d%n",
                            tmId, task.getTaskId(), taskSlots.availablePermits());
                    task.setStatus(TaskStatus.DEPLOYING);

                    // 2. 执行任务
                    return task.execute();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e); // 在 CompletableFuture 中，最好抛出 CompletionException
                } catch (Exception e) {
                    // 捕获任务执行的异常
                    throw new CompletionException(e);
                } finally {
                    // 3. 释放 "Slot"
                    taskSlots.release();
                    System.out.printf("[TM-%s] Slot released by task %s. Available slots: %d%n",
                            tmId, task.getTaskId(), taskSlots.availablePermits());
                }
            }, taskSlotPool); // 指定在自己的线程池中执行
        }

        public void shutdown() {
            taskSlotPool.shutdownNow();
            System.out.printf("[TM-%s] TaskManager has been shut down.%n", tmId);
        }
    }


    // ===================================================================================
    // 3. JobManager: 作业管理者 (Master Node)
    // ===================================================================================

    static class JobManager {
        /**
         * 【JUC应用】并发集合 (ConcurrentHashMap):
         * 存储已注册的 TaskManager。在真实的 Flink 中，TaskManager 的注册、心跳、下线都是并发事件，
         * 使用 ConcurrentHashMap 可以保证线程安全。
         */
        private final Map<String, TaskManager> registeredTaskManagers = new ConcurrentHashMap<>();

        /**
         * 【JUC应用】阻塞队列 (BlockingQueue):
         * 模拟任务调度队列。当 Job 提交后，其所有 Task 被放入这个队列，等待被调度。
         * 调度器线程可以安全地从这个队列中取出任务进行分发，如果队列为空，线程会阻塞等待，避免空轮询。
         */
        private final BlockingQueue<Task> taskDispatchQueue = new LinkedBlockingQueue<>();

        /**
         * 【JUC应用】原子类 (AtomicInteger):
         * 用于生成全局唯一的作业ID，避免在多线程环境下使用 `i++` 造成的竞态条件。
         */
        private final AtomicInteger jobCounter = new AtomicInteger(0);

        // JobManager 自身的执行器，用于处理作业提交和任务调度等内部管理工作
        private final ExecutorService jobManagerExecutor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "job-manager-thread"));

        private volatile boolean isRunning = true;

        /**
         * 【JUC应用】锁 (ReentrantLock) 和 条件 (Condition):
         * 用于实现一个优雅的启动/关闭同步点。外部线程可以等待 JobManager 完全准备就绪后再提交任务。
         * Condition 的 await/signal 机制是比 Object.wait/notify 更灵活的线程间通信方式。
         */
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition systemReadyCondition = lock.newCondition();
        private boolean systemReady = false;

        public JobManager() {
            startTaskDispatcher();
        }

        public void waitUntilReady() throws InterruptedException {
            lock.lock();
            try {
                while (!systemReady) {
                    System.out.println("[JM] Client thread is waiting for JobManager to be ready...");
                    systemReadyCondition.await();
                }
            } finally {
                lock.unlock();
            }
            System.out.println("[JM] JobManager is ready to accept jobs.");
        }

        private void startTaskDispatcher() {
            jobManagerExecutor.submit(() -> {
                // 模拟启动过程
                System.out.println("[JM] JobManager is starting...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                lock.lock();
                try {
                    systemReady = true;
                    // 通知可能正在等待的线程（例如主线程）
                    systemReadyCondition.signalAll();
                    System.out.println("[JM] JobManager started. Signalling ready condition.");
                } finally {
                    lock.unlock();
                }

                // 任务调度循环
                while (isRunning) {
                    try {
                        // 从队列中获取任务，如果队列为空则阻塞
                        Task taskToDispatch = taskDispatchQueue.take();

                        // 简单的轮询调度策略
                        findAvailableTaskManager().ifPresent(tm -> {
                            System.out.printf("[JM] Dispatching task %s to TaskManager %s.%n",
                                    taskToDispatch.getTaskId(), tm.tmId);
                            tm.executeTask(taskToDispatch); // 分发任务，不关心结果，结果由提交Job的Future处理
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("[JM] Task dispatcher thread interrupted.");
                        break;
                    }
                }
            });
        }

        private java.util.Optional<TaskManager> findAvailableTaskManager() {
            // 简单轮询分发
            if (registeredTaskManagers.isEmpty()) {
                System.err.println("[JM] No TaskManagers registered!");
                return java.util.Optional.empty();
            }
            List<TaskManager> managers = new ArrayList<>(registeredTaskManagers.values());
            // 简单轮询
            return java.util.Optional.of(managers.get(new Random().nextInt(managers.size())));
        }

        public void registerTaskManager(String tmId, TaskManager taskManager) {
            registeredTaskManagers.put(tmId, taskManager);
            System.out.printf("[JM] TaskManager %s registered. Total TMs: %d%n",
                    tmId, registeredTaskManagers.size());
        }

        public CompletableFuture<JobStatus> submitJob(Job job) {
            final int totalTasks = job.tasks.size();
            System.out.printf("[JM] Job '%s' submitted with %d tasks.%n", job.jobName, totalTasks);

            /**
             * 【JUC应用】倒计时门闩 (CountDownLatch):
             * 这是等待多个并发事件完成的经典工具。我们用它来等待一个 Job 内的所有 Task 执行完毕（无论成功或失败）。
             * JobManager 在分发完所有任务后，可以在这个 Latch 上等待，或者将其集成到 CompletableFuture 的回调中。
             */
            CountDownLatch completionLatch = new CountDownLatch(totalTasks);

            CompletableFuture<JobStatus> jobFuture = new CompletableFuture<>();

            for (Task task : job.tasks) {
                // 将任务放入调度队列
                taskDispatchQueue.add(task);

                // 注意：在实际的Flink中，任务执行结果会通过RPC返回给JobManager。
                // 这里我们简化一下，通过一个模拟的 "TaskExecution" CompletableFuture 来跟踪每个任务。
                // 这个模拟依赖于TaskManager返回的future，并在其完成时触发latch。

                // 这是模拟的核心：如何知道TM上的任务完成了？
                // 真实的Flink有复杂的RPC和心跳。这里我们通过一个技巧来模拟：
                // 假设JobManager能拿到每个任务执行的Future（虽然在我们的代码里分发后就脱离了直接联系）
                // 我们通过轮询任务状态来模拟这个过程，并在任务完成后递减latch
                // 更真实的模拟应该是TM执行完任务后，通过一个回调（比如调用JM的reportTaskFinished）来通知JM
                // 但为了在一个类里演示，我们用下面的方式，它也能体现异步等待的思想
            }

            // 启动一个监控线程来检查任务状态并完成JobFuture
            startJobMonitor(job, completionLatch, jobFuture);

            return jobFuture;
        }

        // 模拟 Job 的状态监控
        private void startJobMonitor(Job job, CountDownLatch latch, CompletableFuture<JobStatus> jobFuture) {
            new Thread(() -> {
                job.tasks.forEach(task -> {
                    // 在一个真实系统中，这将是异步回调
                    // 这里我们轮询状态来模拟这个异步回调的过程
                    new Thread(() -> {
                        while (task.status.get() != TaskStatus.FINISHED && task.status.get() != TaskStatus.FAILED) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        }
                        // 当一个任务结束（成功或失败），latch减一
                        latch.countDown();
                    }).start();
                });

                try {
                    // 等待所有任务结束
                    latch.await();
                    // 检查最终结果
                    boolean anyFailed = job.tasks.stream()
                            .anyMatch(t -> t.status.get() == TaskStatus.FAILED);
                    if (anyFailed) {
                        System.out.printf("[JM] Job '%s' finished with failures.%n", job.jobName);
                        jobFuture.complete(JobStatus.FAILED);
                    } else {
                        System.out.printf("[JM] Job '%s' finished successfully.%n", job.jobName);
                        jobFuture.complete(JobStatus.FINISHED);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    jobFuture.completeExceptionally(e);
                }
            }, "job-monitor-" + job.jobName).start();
        }

        public void shutdown() {
            isRunning = false;
            jobManagerExecutor.shutdownNow();
            registeredTaskManagers.values().forEach(TaskManager::shutdown);
            System.out.println("[JM] JobManager has been shut down.");
        }
    }


    // ===================================================================================
    // 4. 主程序: 客户端与启动入口
    // ===================================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("--- Flink JUC Simulation Starting ---");

        // 1. 创建和启动 JobManager
        JobManager jobManager = new JobManager();

        /**
         * 【JUC应用】线程间通信 (CyclicBarrier):
         * 模拟一个协调启动的场景。我们要求所有 TaskManager 必须都向 JobManager 注册完毕后，
         * Client 才能开始提交 Job。CyclicBarrier 非常适合这种“所有人都到齐了再一起开始”的场景。
         * 这里我们让主线程和所有TM的注册线程在此同步。
         */
        int tmCount = 2;
        CyclicBarrier registrationBarrier = new CyclicBarrier(tmCount + 1); // 2个TM + 1个主线程

        // 2. 创建和启动 TaskManager
        TaskManager tm1 = new TaskManager("tm-1", 4, jobManager); // TM-1有4个slot
        TaskManager tm2 = new TaskManager("tm-2", 2, jobManager); // TM-2有2个slot

        // 启动注册线程
        new Thread(() -> {
            try {
                tm1.register();
                registrationBarrier.await(); // TM-1 报到
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        new Thread(() -> {
            try {
                tm2.register();
                registrationBarrier.await(); // TM-2 报到
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        // 3. 等待系统就绪
        jobManager.waitUntilReady(); // 等待JM内部状态就绪
        System.out.println("[Client] Waiting for all TaskManagers to register...");
        registrationBarrier.await(); // 主线程等待所有TM报到
        System.out.println("[Client] All TaskManagers registered. System is ready!");


        // 4. 创建并提交 Job
        Job job = new Job("word-count-job", 10); // 创建一个有10个任务的作业
        CompletableFuture<JobStatus> jobResultFuture = jobManager.submitJob(job);

        System.out.println("[Client] Job submitted. Waiting for completion...");

        // 5. 异步等待结果并处理
        jobResultFuture.whenComplete((status, throwable) -> {
            if (throwable != null) {
                System.out.printf("[Client] Job execution failed with exception: %s%n", throwable.getMessage());
            } else {
                System.out.printf("[Client] Job finished with final status: %s%n", status);
            }
        });

        // 阻塞主线程直到Job完成，以便观察所有日志
        JobStatus finalStatus = jobResultFuture.get();
        System.out.println("--- Final Job Status Received by Client: " + finalStatus + " ---");

        // 6. 优雅关闭系统
        jobManager.shutdown();

        System.out.println("--- Flink JUC Simulation Finished ---");
    }
}