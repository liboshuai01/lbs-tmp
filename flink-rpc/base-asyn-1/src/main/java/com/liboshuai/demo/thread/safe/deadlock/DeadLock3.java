package com.liboshuai.demo.thread.safe.deadlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DeadLock3 {
    public static void main(String[] args) {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        new Thread(() -> {
            lock1.lock();
            try {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + ": 进入锁一");
                lock2.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + ": 进入锁二");
                } finally {
                    lock2.unlock();
                }
            } finally {
                lock1.unlock();
            }
        }, "线程1").start();
        new Thread(() -> {
            lock2.lock();
            try {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + ": 进入锁二");
                lock1.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + ": 进入锁一");
                } finally {
                    lock1.unlock();
                }
            } finally {
                lock2.unlock();
            }
        }, "线程2").start();
    }
}
