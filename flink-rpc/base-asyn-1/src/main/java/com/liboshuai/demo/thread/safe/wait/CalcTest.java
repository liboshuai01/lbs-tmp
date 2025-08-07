package com.liboshuai.demo.thread.safe.wait;

import java.util.concurrent.TimeUnit;

public class CalcTest {
    public static void main(String[] args) throws InterruptedException {
        Calc calc = new Calc();
        Thread t1 = new Thread(new CalcTask(true, calc), "加法线程");
        Thread t2 = new Thread(new CalcTask(false, calc), "减法线程");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}

class Calc {
    private int m = 0;

    public synchronized void add() {
        while (m != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        m++;
        System.out.printf("[%s] 执行了一次加一，现 m 值为: [%d]%n", Thread.currentThread().getName(), m);
        notifyAll();
    }

    public synchronized void sub() {
        while (m != 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        m--;
        System.out.printf("[%s] 执行了一次减一，现 m 值为: [%d]%n", Thread.currentThread().getName(), m);
        notifyAll();
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