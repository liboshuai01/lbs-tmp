package com.liboshuai.demo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Test02 {
    public static void main(String[] args) {
        method01();
    }

    private static void method01() {
        Thread t1 = new Thread(() -> {
            System.out.println("进入method01方法");
            System.out.println("park...");
            LockSupport.park();
            System.out.println("unpark...，打断标识: " + Thread.currentThread().isInterrupted());
//            System.out.println("unpark...，打断标识: " + Thread.interrupted());
            LockSupport.park();
            System.out.println("再次unpark...");
        }, "t1");
        t1.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t1.interrupt();
    }
}
