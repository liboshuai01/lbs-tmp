package com.liboshuai.demo.pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// 简单的线程池实现
public class SimpleThreadPool implements ThreadPool{
    // 线程池初始化时的线程数量
    private int initialSize;

    // 任务队列
    private BlockingQueue<Runnable> taskQueue;

    // 用于存放和管理工作线程的集合
    private List<WorkerThread> threads;

    // 是否已经被shutdown的标志
    private volatile boolean isShutdown = false;

    public SimpleThreadPool(int initialSize) {
        this.initialSize = initialSize;
        taskQueue = new LinkedBlockingQueue<>();
        threads = new ArrayList<>(initialSize);
        // 初始化方法，创建一定数量的工作线程，并启动它们
        for (int i = 0; i < initialSize; i++) {
            WorkerThread workerThread = new WorkerThread(taskQueue);
            workerThread.start();
            threads.add(workerThread);
        }
    }


    // 实现 execute 方法，用于将任务加入到任务队列，并通知工作线程来执行
    @Override
    public void execute(Runnable task) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool is shutdown!");
        }
        boolean ignore = taskQueue.offer(task);
    }

    // 关闭线程池，等待所有线程执行完毕
    @Override
    public void shutdown() {
        // 修改状态
        isShutdown = true;
        for (WorkerThread thread : threads) {
            // 中断线程
            thread.interrupt();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        // 修改状态
    }
}
