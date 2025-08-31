package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 初步解决线程复用的问题
 * 存在的问题：
 * 1. 线程池无法关闭
 * 2. 任务挤压，会出现丢失的问题
 * 3. 没有拒绝策略
 * 4. 没有非核心线程功能
 */
@Slf4j
public class LbsThreadPoolExecutor {

    public static void main(String[] args) {
        LbsThreadPoolExecutor lbsThreadPoolExecutor = new LbsThreadPoolExecutor(3, new LinkedBlockingQueue<Runnable>(10));
        for (int i = 0; i < 30; i++) {
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

    private final BlockingQueue<Runnable> workQueue;

    /**
     * 自定义线程池构造函数
     * @param poolSize 核心线程数
     * @param workQueue 任务队列
     */
    public LbsThreadPoolExecutor(int poolSize, BlockingQueue<Runnable> workQueue) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize必须大于0");
        }
        if (workQueue == null) {
            throw new NullPointerException("workQueue必须非空");
        }
        this.workQueue = workQueue;
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            worker.thread.start();
        }
    }

    /**
     * 提交任务的入口方法
     * @param task 提交的任务
     */
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("提交到线程池的任务对象不能为null");
        }
        boolean offer = workQueue.offer(task);
        if (offer) {
            log.info("提交任务到队列成功");
        } else {
            // 等待后续版本升级，提供创建非核心线程来进行消费
            log.warn("提交任务到队列失败");
        }
    }

    /**
     * 私有不变内部类，代表一个真正的工作线程
     */
    private final class Worker implements Runnable {

        final Thread thread;

        public Worker() {
            thread = new Thread(this);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Runnable take = workQueue.take();
                    take.run();
                } catch (InterruptedException e) {
                    // V1 版本先忽略，后续中断就是我们关闭线程池的关键
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
