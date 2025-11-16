package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSimulator2 {
    
    private static final Logger log = LoggerFactory.getLogger(TaskSimulator2.class);
    
    static class StreamTask implements Runnable {

        private volatile boolean isRunning = true;

        private final AtomicInteger counter = new AtomicInteger(0);

        private final CountDownLatch countDownLatch;

        public StreamTask(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    int count = counter.incrementAndGet();
                    log.info("任务正在运行, Cnt: " + count);
                }
            } finally {
                log.info("收到停止信号, 退出循环。");
                countDownLatch.countDown();
            }
        }

        public void stop() {
            log.info("正在发送停止信号...");
            isRunning = false;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        int taskNums = 3;

        CountDownLatch countDownLatch = new CountDownLatch(taskNums);

        StreamTask streamTask = new StreamTask(countDownLatch);

        ExecutorService ioExecutor = Executors.newFixedThreadPool(taskNums);
        for (int i = 0; i < 3; i++) {
            ioExecutor.submit(streamTask);
        }

        TimeUnit.SECONDS.sleep(1);
        streamTask.stop();

        if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
            log.warn("等待任务完成超时! 可能有任务卡住了。");
        } else {
            log.info("所有任务均已报告完成!");
            log.info("所有线程共同处理了 " + streamTask.getCounter() + " 条数据");
        }

        ExecutorUtils.close(ioExecutor, 10, TimeUnit.SECONDS);
    }
}
