package com.liboshuai.demo.thread.safe.deadlock;

import java.util.concurrent.TimeUnit;

public class DeadLockTest {
    public static void main(String[] args) throws InterruptedException {
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();

        Thread t1 = new Thread(new R1(s1, s2));
        Thread t2 = new Thread(new R2(s1, s2));
        t1.start();
        t2.start();

        System.out.println("主线程等待异步执行完毕");
        t1.join();
        t2.join();
        System.out.println("异步执行完毕啦！");

    }
}

class R1 implements Runnable {

    private final StringBuilder s1;

    private final StringBuilder s2;

    public R1(StringBuilder s1, StringBuilder s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public void run() {
        synchronized (s1) {
            s1.append("a");
            s2.append("1");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (s2) {
                s1.append("b");
                s2.append("2");

                System.out.println(s1);
                System.out.println(s2);
            }
        }
    }
}

class R2 implements Runnable {

    private final StringBuilder s1;

    private final StringBuilder s2;

    public R2(StringBuilder s1, StringBuilder s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public void run() {
        synchronized (s2) {
            s1.append("c");
            s2.append("3");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (s1) {
                s1.append("d");
                s2.append("4");

                System.out.println(s1);
                System.out.println(s2);
            }
        }
    }
}