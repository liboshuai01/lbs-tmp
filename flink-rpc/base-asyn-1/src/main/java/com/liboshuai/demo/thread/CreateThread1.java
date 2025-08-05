package com.liboshuai.demo.thread;

/**
 * 演示创建线程的两种方式
 * 方式一：继承 Thread 类
 */
public class CreateThread1 {
    public static void main(String[] args) {
        new MyThreadOdd().start();
        new MyThreadEven().start();
        for (int i = 0; i < 10000; i++) {
            System.out.printf("主线程 [%s] 打印所有数: %d%n", Thread.currentThread().getName(), i);
        }
    }
}

class MyThreadOdd extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 1) {
                System.out.printf("线程 [%s] 打印奇数: %d%n", Thread.currentThread().getName(), i);
            }
        }
    }
}

class MyThreadEven extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 0) {
                System.out.printf("线程 [%s] 打印偶数: %d%n", Thread.currentThread().getName(), i);
            }
        }
    }
}