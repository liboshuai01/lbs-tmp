package com.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
// 定义一个工作线程类
public class WorkerThread extends Thread{
    // 用于从任务队列中取出并执行任务
    private BlockingQueue<Runnable> taskQueue;

    // 构造方法，传入任务队列
    public WorkerThread(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    // 重写 run 方法
    @Override
    public void run() {
        // 循环执行，直到线程被中断
        while (!Thread.currentThread().isInterrupted() && !taskQueue.isEmpty()) {
            try {
                // 从任务队列中取出一个任务，如果队列为空，则阻塞等待
                Runnable task = taskQueue.take();
            } catch (Exception e) {
                // 如果线程被中断，则退出循环
                break;
            }
        }
    }
}
