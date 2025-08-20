package com.liboshuai.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test15 {

    static boolean flag = false;
    public static void main(String[] args) throws InterruptedException {
        final Object lock = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                while(!flag) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("我是TaskA...");
            }
        }, "线程A");
        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("我是TaskB...");
                flag = true;
                lock.notifyAll();
            }
        }, "线程B");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
