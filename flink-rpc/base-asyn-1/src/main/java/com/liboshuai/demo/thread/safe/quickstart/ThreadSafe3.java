package com.liboshuai.demo.thread.safe.quickstart;

/**
 * 演示线程安全问题，解决方法二，使用synchronized
 * 但是减少锁的粒度，可以让更多的线程参与到票的出售中
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

    private int count = 10000;

    @Override
    public void run() {
        while (count > 0) {
            // 每次循环都争抢一次锁，锁的粒度变小
            synchronized (this) {
                // 双重检查，防止一个线程在等待锁时，票已经被卖完了
                if (count > 0) {
                    count--;
                    System.out.printf("线程 [%s] 卖出一张票，剩余: %d%n", Thread.currentThread().getName(), count);
                }
            }
        }
    }
}
