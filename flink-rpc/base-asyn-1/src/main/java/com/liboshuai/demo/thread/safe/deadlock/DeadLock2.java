package com.liboshuai.demo.thread.safe.deadlock;

import java.util.concurrent.TimeUnit;

public class DeadLock2 implements Runnable{
    Task1 task1 = new Task1();
    Task2 task2 = new Task2();

    public void init() {
        task1.foo(task2);
        System.out.println("进入主线程之后");
    }

    @Override
    public void run() {
        task2.foo(task1);
        System.out.println("进入副线程之后");
    }

    public static void main(String[] args) {
        DeadLock2 deadLock2 = new DeadLock2();
        new Thread(deadLock2).start();
        deadLock2.init();
    }

}


class Task1 {
    public synchronized void foo(Task2 task) {
        System.out.println(Thread.currentThread().getName() + "进入了Task1的foo方法");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "在Task1中试图调用Task2中的last方法");
        task.last();
    }

    public synchronized void last() {
        System.out.println("进入了Task1类的last方法");
    }
}

class Task2 {
    public synchronized void foo(Task1 task) {
        System.out.println(Thread.currentThread().getName() + "进入了Task2的foo方法");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "在Task2中试图调用Task1中的last方法");
        task.last();
    }

    public synchronized void last() {
        System.out.println("进入了Task2类的last方法");
    }
}