package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 版本 V1: 核心功能版 - 固定线程数的执行器
 * 目标：
 * 1. 在构造时创建一组固定数量的工作线程
 * 2. 提供 execute 方法接收任务
 * 3. 工作线程从一个共享的阻塞队列中获取并执行任务
 * 4. 实现最核心的“线程复用”
 */
@Slf4j
public class LbsThreadPoolExecutor {

    public static void main(String[] args) throws InterruptedException {
        LbsThreadPoolExecutor lbsThreadPoolExecutor = new LbsThreadPoolExecutor(3, new LinkedBlockingQueue<>(10));
        for (int i = 1; i < 31; i++) {
            int num = i;
            lbsThreadPoolExecutor.execute(() -> {
                log.info("任务-{}开始执行", num);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("任务-{}执行完毕", num);
            });
        }
    }

    // 任务队列
    private final BlockingQueue<Runnable> workQueue;

    /**
     * 构造函数
     */
    public LbsThreadPoolExecutor(int poolSize, BlockingQueue<Runnable> workQueue) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize必须要大于0");
        }
        if (workQueue == null) {
            throw new NullPointerException("workQueue必须非空");
        }
        this.workQueue = workQueue;
        // 预先创建并启动所有工作线程
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            // 启动工作线程，此时它们会阻塞在 workQueue.take() 方法上
            worker.thread.start();
        }
    }

    /**
     * 执行任务的唯一入口
     * @param task 需要执行的任务
     */
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("提交的任务不能为null");
        }
        // 直接将任务放入队列，等待工作线程来取
        // offer 如果队列满了会返回 false，这里我们暂时不处理
        boolean ignore = workQueue.offer(task);
        if (ignore) {
            log.info("新提交的任务放入到任务队列中-成功");
        } else {
            // V1 版本，在任务队列满后直接抛弃后续提交的其他任务
            log.warn("新提交的任务放入到任务队列中-失败");
        }
    }


    /**
     * Worker 内部类，代表一个真正的工作线程
     */
    private final class Worker implements Runnable {

        // Worker 持有自己的线程，方便管理
        final Thread thread;

        Worker() {
            // 在构造时就创建一个线程，并将 Worker 自身（this）作为任务
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
                    // 在后续版本中，中断将是我们关闭线程池的关键
                    log.error("", e);
                }
            }
        }
    }
}
