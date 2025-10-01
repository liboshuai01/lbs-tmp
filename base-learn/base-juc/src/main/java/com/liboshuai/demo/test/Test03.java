package com.liboshuai.demo.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Test03 {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        // NEW
        Thread t1 = new Thread(() -> {}, "t1");
        // RUNNABLE
        Thread t2 = new Thread(() -> {
            while (true) {

            }
        }, "t2");
        // TERMINATED
        Thread t3 = new Thread(() -> {

        }, "t3");
        // TIMED_WAITING
        Thread t4 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "t4");
        // WAITING
        Thread t5 = new Thread(() -> {
            LockSupport.park();
        }, "t5");
        // BLOCKED
        Thread t6 = new Thread(() -> {
            synchronized (lock) {

            }
        }, "t6");

        System.out.println(t1.getState()); // NEW

        t2.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(t2.getState()); // RUNNABLE

        t3.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(t3.getState()); // TERMINATED

        t4.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(t4.getState()); // TIMED_WAITING

        t5.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(t5.getState()); // WAITING

        synchronized (lock) {
            t6.start();
            TimeUnit.SECONDS.sleep(1);
            System.out.println(t6.getState()); // BLOCKED
        }
    }
}
