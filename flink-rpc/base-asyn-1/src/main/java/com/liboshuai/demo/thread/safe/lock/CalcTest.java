package com.liboshuai.demo.thread.safe.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CalcTest {
    public static void main(String[] args) {
        Calc calc = new Calc();
        Thread addThread = new Thread(new CalcTask(true, calc), "加法线程");
        Thread subThread = new Thread(new CalcTask(false, calc), "减法线程");
        addThread.start();
        subThread.start();
    }
}

class Calc {
    private int m = 0;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition addCondition = lock.newCondition();

    private final Condition subCondition = lock.newCondition();


    public void add() {
        lock.lock();
        try {
            while (m != 0) {
                System.out.printf("[%s] 不满足加法条件，阻塞等待被唤醒%n", Thread.currentThread().getName());
                try {
                    addCondition.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            m++;
            System.out.printf("[%s] 执行了加法，现在 m 值为: [%d]，并主动唤醒减法线程%n", Thread.currentThread().getName(), m);
            subCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void sub() {
        lock.lock();
        try {
            while (m != 1) {
                System.out.printf("[%s] 不满足减法条件，阻塞等待被唤醒%n", Thread.currentThread().getName());
                try {
                    subCondition.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            m--;
            System.out.printf("[%s] 执行了减法，现在 m 值为: [%d]，并主动唤醒加法线程%n", Thread.currentThread().getName(), m);
            addCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class CalcTask implements Runnable {

    private final boolean isAdd;

    private final Calc calc;

    public CalcTask(boolean isAdd, Calc calc) {
        this.isAdd = isAdd;
        this.calc = calc;
    }

    @Override
    public void run() {
        while (true) {
            if (isAdd) {
                calc.add();
            } else {
                calc.sub();
            }
        }
    }
}