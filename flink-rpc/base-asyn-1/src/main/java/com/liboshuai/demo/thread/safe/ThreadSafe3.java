package com.liboshuai.demo.thread.safe;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示线程安全问题
 */
public class ThreadSafe3 {
    public static void main(String[] args) {
        MyRunnable3 runnable = new MyRunnable3();

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        Thread thread3 = new Thread(runnable);

        thread1.start();
        thread2.start();
        thread3.start();
    }
}

class MyRunnable3 implements Runnable {

    private final AtomicInteger count = new AtomicInteger(100);

    @Override
    public void run() {
        int currentCount;
        while ((currentCount = count.decrementAndGet()) >= 0) {
            System.out.printf("线程 [%s] 卖出一张票，剩余: %d%n", Thread.currentThread().getName(), currentCount);
        }
    }
}
