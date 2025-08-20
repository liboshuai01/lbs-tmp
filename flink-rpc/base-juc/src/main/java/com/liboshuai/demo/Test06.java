package com.liboshuai.demo;

import java.util.concurrent.TimeUnit;

public class Test06 {
    private static int num = 0;

    public static void main(String[] args) throws InterruptedException {

        Object lock = new Object();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                synchronized (lock) {
                    num++;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                synchronized (lock) {
                    num--;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("最终num的值为: " + num);
    }


}
