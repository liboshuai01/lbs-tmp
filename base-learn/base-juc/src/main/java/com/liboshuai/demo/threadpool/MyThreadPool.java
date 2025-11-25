package com.liboshuai.demo.threadpool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 手写简易线程池 (MyThreadPool)
 * 具备功能：
 * 1. 核心线程与非核心线程区分
 * 2. 任务队列缓冲
 * 3. 动态扩容与超时回收
 * 4. 拒绝策略
 * 5. 优雅关闭 (Shutdown)
 */
@Slf4j
public class MyThreadPool {

    // --- 核心配置参数 ---

    // 任务队列 (暴露给拒绝策略使用，如 DiscardOldest)
    @Getter
    private final MyBlockingQueue<Runnable> queue;

    // 核心线程数
    private final int corePoolSize;

    // 最大线程数
    private final int maximumPoolSize;

    // 非核心线程的空闲存活时间
    private final long keepAliveTime;
    private final TimeUnit unit;

    // 拒绝策略
    private final MyRejectPolicy rejectPolicy;

    // --- 内部状态 ---

    // 存放所有的 Worker (工作线程包装类)
    private final Set<Worker> workers = new HashSet<>();

    // 主锁：保护 workers 集合的并发安全 (添加/删除 worker 时使用)
    private final ReentrantLock mainLock = new ReentrantLock();

    // 线程池状态标志：是否处于关闭状态
    private volatile boolean isShutdown = false;

    /**
     * 构造函数
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                        int queueCapacity, MyRejectPolicy rejectPolicy) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.queue = new MyBlockingQueue<>(queueCapacity);
        this.rejectPolicy = rejectPolicy;
    }

    /**
     * 提交任务的核心入口
     * 逻辑顺序：Core -> Queue -> Max -> Reject
     */
    public void execute(Runnable task) {
        Objects.requireNonNull(task, "任务不能为空");

        // 如果线程池已经关闭，禁止提交
        if (isShutdown) {
            throw new IllegalStateException("线程池已关闭，无法提交任务");
        }

        // 1. 如果当前线程数 < 核心线程数，直接创建核心线程执行
        if (getWorkerCount() < corePoolSize) {
            if (addWorker(task, true)) {
                return;
            }
            // 如果创建失败(可能并发下被别人抢了)，继续往下走
        }

        // 2. 尝试放入阻塞队列 (非阻塞尝试，或者带极短超时)
        // 这里使用 0 时间，意味着"能放进就放，放不进拉倒"，不阻塞主线程
        try {
            if (queue.offer(task, 0, TimeUnit.NANOSECONDS)) {
                log.debug("任务进入队列等待: {}", task);
                return;
            }
        } catch (InterruptedException e) {
            log.error("任务入队被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            return;
        }

        // 3. 队列满了，尝试创建非核心线程 (扩容)
        // addWorker 第二个参数 false 表示创建的是非核心线程
        if (!addWorker(task, false)) {
            // 4. 扩容也失败了 (达到了 maximumPoolSize)，执行拒绝策略
            rejectPolicy.reject(task, this);
        }
    }

    /**
     * 添加一个 Worker 线程
     * @param firstTask 首个要执行的任务
     * @param isCore 是否作为核心线程创建 (用于判断数量限制)
     * @return true 创建成功, false 失败 (超限或关闭)
     */
    private boolean addWorker(Runnable firstTask, boolean isCore) {
        // 必须要加锁，因为 workers 是非线程安全的 HashSet
        mainLock.lock();
        try {
            // 再次检查状态，如果关闭了就不再创建
            if (isShutdown) return false;

            int currentSize = workers.size();
            int limit = isCore ? corePoolSize : maximumPoolSize;

            if (currentSize >= limit) {
                return false; // 超过限制
            }

            // 创建并启动 Worker
            Worker newWorker = new Worker(firstTask);
            workers.add(newWorker);
            newWorker.thread.start();
            log.info("创建新线程 [Core={}]: {}", isCore, newWorker.thread.getName());
            return true;

        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 优雅关闭线程池
     * 1. 停止接收新任务 (isShutdown = true)
     * 2. 中断空闲线程 (正在 queue.take() 阻塞的线程)
     * 3. 忙碌线程会把手头的和队列里的任务做完再退出
     */
    public void shutdown() {
        mainLock.lock();
        try {
            if (isShutdown) return;
            isShutdown = true;
            log.info("=== 线程池开始执行 Shutdown 流程 ===");

            // 遍历所有 Worker，中断那些"闲着"的
            for (Worker w : workers) {
                if (!w.isWorking) { // isWorking 变量在这里发挥作用
                    w.thread.interrupt();
                    log.debug("中断空闲线程: {}", w.thread.getName());
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 获取任务 (Worker 的核心逻辑之一)
     * 负责判断线程是否应该销毁 (超时回收)
     */
    private Runnable getTask() {
        boolean timeOut = false; // 记录上一次 poll 是否超时

        for (;;) {
            // --- 判断关闭逻辑 ---
            // 如果线程池关闭了，且队列空了，线程就不需要存在了，返回 null 让 Worker 退出
            if (isShutdown && queue.size() == 0) {
                return null;
            }

            int currentSize = getWorkerCount();
            // 判断是否需要超时回收：
            // 只要当前线程数 > corePoolSize，所有线程都可能被回收 (JDK 策略)
            boolean allowTimeOut = currentSize > corePoolSize;

            // 如果允许超时，且上一次已经超时了，说明该线程空闲太久，申请销毁
            if (allowTimeOut && timeOut) {
                return null;
            }

            try {
                // 如果允许超时，用 poll(keepAliveTime)；否则用 take() 死等
                Runnable r = allowTimeOut ?
                        queue.poll(keepAliveTime, unit) :
                        queue.take();

                if (r != null) {
                    return r; // 拿到任务，回去干活
                }

                // 走到这里说明 r == null，也就是 poll 超时了
                timeOut = true;
            } catch (InterruptedException e) {
                // 捕获中断异常
                // 如果是 shutdown() 发起的中断，下一次循环头部的 if (isShutdown) 会处理退出
                timeOut = false;
            }
        }
    }

    /**
     * Worker 的运行循环
     */
    private void runWorker(Worker worker) {
        Runnable task = worker.firstTask;
        worker.firstTask = null; // 释放引用

        try {
            // 循环条件：手头有任务 OR 能从队列里取到任务
            // getTask() 返回 null 时，循环结束，线程销毁
            while (task != null || (task = getTask()) != null) {
                // 标记为忙碌状态，防止 shutdown 时被误杀
                worker.isWorking = true;
                try {
                    task.run(); // 执行用户业务逻辑
                } catch (Exception e) {
                    log.error("任务执行发生未捕获异常", e);
                } finally {
                    task = null; // 至关重要！置空以便下次 getTask
                    worker.isWorking = false; // 标记为空闲
                }
            }
        } finally {
            // 退出循环后的收尾工作
            processWorkerExit(worker);
        }
    }

    /**
     * Worker 退出清理
     */
    private void processWorkerExit(Worker worker) {
        mainLock.lock();
        try {
            workers.remove(worker);
            log.info("线程销毁退出: {}, 当前剩余线程数: {}", worker.thread.getName(), workers.size());
        } finally {
            mainLock.unlock();
        }
    }

    public int getWorkerCount() {
        mainLock.lock();
        try {
            return workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    // --- 内部类 Worker (工人) ---
    private class Worker implements Runnable {
        final Thread thread;
        Runnable firstTask;
        // 标识是否正在执行任务 (volatile 保证可见性)
        volatile boolean isWorking = false;

        public Worker(Runnable firstTask) {
            this.firstTask = firstTask;
            // 将 Worker 自己作为 Runnable 传给 Thread，thread.start() 会调用 Worker.run()
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }
}