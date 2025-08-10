package com.liboshuai.demo.safe.quickstart;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示线程安全问题，解决方法三，使用原子类（cas原理）
 */
public class ThreadSafe4 {
    public static void main(String[] args) {
        MyRunnable4 runnable = new MyRunnable4();

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        Thread thread3 = new Thread(runnable);

        thread1.start();
        thread2.start();
        thread3.start();
    }
}

class MyRunnable4 implements Runnable {

    private final AtomicInteger count = new AtomicInteger(10000);

    @Override
    public void run() {
        int currentCount;
        while ((currentCount = count.decrementAndGet()) >= 0) {
            System.out.printf("线程 [%s] 卖出一张票，剩余: %d%n", Thread.currentThread().getName(), currentCount);
        }
    }
}
