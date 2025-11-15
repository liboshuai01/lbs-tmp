package com.liboshuai.demo.juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSimulator {
    static class StreamTask implements Runnable {

        private volatile boolean isRunning = true;

        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void run() {
            while (isRunning) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter.incrementAndGet();
                System.out.println("任务正在运行, Cnt: " + counter);
            }
            System.out.println(Thread.currentThread().getName() + " 收到停止信号, 退出循环。");
            System.out.println("总共处理了 " + counter + " 条数据。");
        }

        public void stop() {
            System.out.println(Thread.currentThread().getName() + " 正在发送停止信号...");
            isRunning = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        StreamTask streamTask = new StreamTask();

        Thread taskThread = new Thread(streamTask, "TaskThread");
        taskThread.start();

        TimeUnit.SECONDS.sleep(1);
        streamTask.stop();

        taskThread.join();
        System.out.println(Thread.currentThread().getName() + " 确认任务已停止。");
    }
}
