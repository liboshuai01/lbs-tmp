package com.liboshuai.demo.thread.safe.quickstart;

/**
 * 演示线程安全问题
 */
public class ThreadSafe1 {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        Thread thread3 = new Thread(runnable);

        thread1.start();
        thread2.start();
        thread3.start();
    }
}

class MyRunnable implements Runnable {

    private int count = 100;

    @Override
    public void run() {
        while (count > 0) {
            count--;
            System.out.printf("线程 [%s] 卖出一张票，剩余: %d%n", Thread.currentThread().getName(), count);
        }
    }
}
