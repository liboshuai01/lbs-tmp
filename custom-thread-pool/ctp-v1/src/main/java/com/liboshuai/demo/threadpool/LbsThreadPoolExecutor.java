package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

/**
 * 版本 V1: 核心功能版 - 固定线程数的执行器
 * 目标:
 * 1. 在构造时创建一组固定数量的工作线程。
 * 2. 提供 execute 方法接收任务。
 * 3. 工作线程从一个共享的阻塞队列中获取并执行任务。
 * 4. 实现最核心的 "线程复用"。
 */
@Slf4j
public class LbsThreadPoolExecutor {

    // 用于存放工作线程的集合
    private final HashSet<Worker> workers = new HashSet<>();

    // 任务队列
    private final BlockingQueue<Runnable> workQueue;

    // 线程池中的线程数
    private final int poolSize;

    /**
     * 构造函数
     * @param poolSize 线程池要创建的固定线程数量
     * @param workQueue 存储任务的阻塞队列
     */
    public LbsThreadPoolExecutor(int poolSize, BlockingQueue<Runnable> workQueue) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0");
        }
        if (workQueue == null) {
            throw new NullPointerException("Work queue cannot be null");
        }
        this.poolSize = poolSize;
        this.workQueue = workQueue;

        // 预先创建并启动所有工作线程
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            // 启动工作线程，此时它们会阻塞在 getTask() 方法上
            worker.thread.start();
        }
    }

    /**
     * 执行任务的唯一入口
     * @param task 需要执行的任务
     */
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        // 直接将任务放入队列，等待工作线程来取
        // offer 如果队列满了会返回 false，这里我们暂不处理
        workQueue.offer(task);
    }

    /**
     * Worker 内部类，代表一个真正的工作线程
     */
    private final class Worker implements Runnable {

        // Worker 持有自己的线程，方便管理
        final Thread thread;

        Worker() {
            // 在构造时就创建一个线程，并将 Worker 自身(this)作为任务
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            // 线程启动后，进入无限循环，不断地从队列中获取任务并执行
            while (true) {
                try {
                    // take() 方法会阻塞，直到队列中有可用的任务
                    Runnable task = workQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    // 在 V1 中，我们暂时忽略中断
                    // 在后续版本中，中断将是关闭线程池的关键
                    log.error("", e);
                }
            }
        }
    }
}