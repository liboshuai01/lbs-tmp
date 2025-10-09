package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class MyThreadPoolExecutor {

    private final int coreSize;
    private final int maxSize;

    public MyThreadPoolExecutor(int coreSize, int maxSize) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
    }

    private final List<Thread> threadList = new ArrayList<>();
    private final List<Thread> tmpThreadList = new ArrayList<>();
    private final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(6);

    private final Runnable task = () -> {
        while (true) {
            try {
                Runnable command = blockingQueue.take();
                command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("提交的线程池任务不能为空");
        }
        if (threadList.size() < coreSize) { // 如果核心线程还没有都被创建
            Thread thread = new Thread(task);
            log.info("线程被创建了");
            threadList.add(thread);
            thread.start();
        }
        // 核心线程不够用了，则加入到阻塞队列；阻塞队列满了，则新增临时线程
        if (!blockingQueue.offer(command)) {
            if (tmpThreadList.size() < (maxSize - coreSize)) { // 还没有达到最大线程数
                Thread thread = new Thread(task);
                log.info("线程被创建了");
                tmpThreadList.add(thread);
                thread.start();
            } else { // 已经达到最大线程数了，需要执行拒绝策略
                log.warn("任务被拒绝了");
            }
        }
    }



    public void shutdown() {

    }
}
