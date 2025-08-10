package com.liboshuai.demo.create;

/**
 * 演示创建线程的两种方式
 * 方式二：实现 Runnable 接口
 */
public class CreateThread2 {
    public static void main(String[] args) {
        new Thread(new MyRunnableOdd()).start();
        new Thread(new MyRunnableEven()).start();
        for (int i = 0; i < 10000; i++) {
            System.out.printf("主线程 [%s] 打印所有数: %d%n", Thread.currentThread().getName(), i);
        }
    }
}

class MyRunnableOdd implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 1) {
                System.out.printf("线程 [%s] 打印奇数: %d%n", Thread.currentThread().getName(), i);
            }
        }
    }
}

class MyRunnableEven implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 0) {
                System.out.printf("线程 [%s] 打印偶数: %d%n", Thread.currentThread().getName(), i);
            }
        }
    }
}