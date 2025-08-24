package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LbsThreadPoolExecutor {

    // --- 线程池状态 ---
    // 使用 AtomicInteger 来保证状态变更的原子性
    private final AtomicInteger runState = new AtomicInteger(RUNNING);
    private static final int RUNNING    = 0; // 接受新任务，处理队列任务
    private static final int SHUTDOWN   = 1; // 不接受新任务，但处理队列任务
    private static final int STOP       = 2; // 不接受新任务，不处理队列任务，中断正在执行的任务

    // --- 核心参数 ---
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit;
    private final BlockingQueue<Runnable> workQueue;
    private final LbsRejectedExecutionHandler handler;
    private final ThreadFactory threadFactory;

    // --- 状态与工作集 ---
    private final AtomicInteger workerCount = new AtomicInteger(0);
    private final HashSet<Worker> workers = new HashSet<>();
    private final ReentrantLock mainLock = new ReentrantLock();


    /**
     * 构造函数 - 支持自定义 ThreadFactory
     */
    public LbsThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory,
                                 LbsRejectedExecutionHandler handler) {
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
            throw new IllegalArgumentException("Invalid thread pool arguments");
        }
        if (workQueue == null || threadFactory == null || handler == null) {
            throw new NullPointerException();
        }
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 构造函数 - 使用默认的 ThreadFactory
     */
    public LbsThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 LbsRejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new DefaultThreadFactory(), handler);
    }

    /**
     * 线程池的核心方法：执行任务
     * @param command 需要执行的任务
     */
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }

        if (runState.get() != RUNNING) {
            reject(command);
            return;
        }

        if (workerCount.get() < corePoolSize) {
            if (addWorker(command, true)) {
                return;
            }
        }

        // 步骤 3: 尝试将任务加入队列
        if (workQueue.offer(command)) {
            // Double-check: 再次检查线程池状态
            if (runState.get() != RUNNING) {
                // 如果线程池已关闭，尝试从队列中移除任务并拒绝
                if (workQueue.remove(command)) {
                    reject(command);
                }
            }
            // 【关键修复】如果入队后没有工作线程，需要创建一个来处理队列任务
            else if (workerCount.get() == 0) {
                // 添加一个 firstTask 为 null 的 worker，它会直接从队列取任务
                addWorker(null, false);
            }
        } else {
            // 步骤 4: 如果队列已满，尝试创建非核心线程
            if (!addWorker(command, false)) {
                // 步骤 5: 如果无法创建非核心线程（已达最大线程数），执行拒绝策略
                reject(command);
            }
        }
    }

    /**
     * 【修正】优雅关闭：不再接受新任务，但会等待队列中的任务执行完毕。
     */
    public void shutdown() {
        runState.compareAndSet(RUNNING, SHUTDOWN);
        // 此处无需中断线程，getTask() 方法会检查状态并让空闲线程自然退出
    }

    /**
     * 【新增】立即关闭：尝试停止所有正在执行的任务，并返回等待执行的任务列表。
     * @return 队列中尚未执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        mainLock.lock();
        try {
            // 推进状态至 STOP
            if (runState.get() < STOP) {
                runState.set(STOP);
            }
            // 中断所有工作线程
            for (Worker w : workers) {
                try {
                    w.thread.interrupt();
                } catch (SecurityException ignore) {
                    // 忽略权限问题
                }
            }
            // 排空工作队列
            List<Runnable> unexecutedTasks = new ArrayList<>();
            workQueue.drainTo(unexecutedTasks);
            return unexecutedTasks;
        } finally {
            mainLock.unlock();
        }
    }

    public boolean isShutdown() {
        return runState.get() != RUNNING;
    }

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    private void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 添加工作线程的核心方法
     * @param firstTask 新线程的第一个任务，可以为 null
     * @param core      是否是核心线程
     * @return true 如果添加成功
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        mainLock.lock();
        try {
            int rs = runState.get();
            // 如果线程池状态不允许添加新任务，则直接返回 false
            if (rs >= SHUTDOWN && (rs >= STOP || firstTask != null || workQueue.isEmpty())) {
                return false;
            }

            int wc = workerCount.get();
            int poolSize = core ? corePoolSize : maximumPoolSize;
            if (wc >= poolSize) {
                return false;
            }

            // 增加工作线程数
            workerCount.incrementAndGet();
            Worker worker = new Worker(firstTask);
            Thread newThread = threadFactory.newThread(worker); // 使用 ThreadFactory 创建线程
            worker.thread = newThread;

            try {
                workers.add(worker);
                newThread.start();
                return true;
            } catch (Throwable ex) {
                // 如果添加或启动失败，必须进行回滚清理
                workers.remove(worker);
                workerCount.decrementAndGet();
                System.err.println("Failed to start a new worker thread.");
                log.error("添加工作线程失败",ex);
                return false;
            }
        } finally {
            mainLock.unlock();
        }
    }

    private void processWorkerExit(Worker w) {
        mainLock.lock();
        try {
            workers.remove(w);
            workerCount.decrementAndGet();
        } finally {
            mainLock.unlock();
        }
    }

    // --- Worker 内部类 ---
    private final class Worker implements Runnable {
        Runnable firstTask;
        // 【修正】thread 字段变为非 final，以便在 addWorker 中创建和赋值
        Thread thread;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }

    private void runWorker(Worker worker) {
        Runnable task = worker.firstTask;
        worker.firstTask = null; // 允许 firstTask 被垃圾回收

        try {
            while (task != null || (task = getTask()) != null) {
                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.err.println("任务执行时发生异常：" + e.getMessage());
                } finally {
                    task = null;
                }
            }
        } finally {
            processWorkerExit(worker);
        }
    }

    /**
     * 工作线程从队列获取任务的核心逻辑
     */
    private Runnable getTask() {
        try {
            int rs = runState.get();
            // 1. 检查线程池状态，如果需要关闭，则直接返回 null 使线程退出
            if (rs >= STOP || (rs == SHUTDOWN && workQueue.isEmpty())) {
                return null;
            }

            // 2. 判断当前工作线程是否需要超时退出
            boolean timed = workerCount.get() > corePoolSize;

            // 3. 根据是否需要超时，选择不同的方法获取任务并直接返回结果
            if (timed) {
                // 非核心线程：使用超时获取。
                // 如果 poll 返回任务，则返回该任务；如果超时返回 null，则返回 null，线程随之退出。
                return workQueue.poll(keepAliveTime, unit);
            } else {
                // 核心线程：阻塞等待任务。
                return workQueue.take();
            }
        } catch (InterruptedException e) {
            // 4. 捕获中断异常（通常由 shutdownNow 触发），返回 null 使线程退出
            return null;
        }
    }

    /**
     * 【新增】默认的线程工厂类，用于创建统一命名的线程
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            this.namePrefix = "lbs-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            // 确保线程不是守护线程
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            // 设置标准优先级
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}