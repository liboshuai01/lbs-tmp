package com.liboshuai.demo.test;

import java.util.concurrent.locks.LockSupport;

public class Test17 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            LockSupport.park();
            System.out.println("A...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            LockSupport.unpark(t1);
            System.out.println("B...");
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
