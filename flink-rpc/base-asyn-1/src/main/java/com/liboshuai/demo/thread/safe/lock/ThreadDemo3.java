package com.liboshuai.demo.thread.safe.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadDemo3 {
    public static void main(String[] args) {
        PrintNumber3 printNumber3 = new PrintNumber3();
        FutureTask<Object> futureTask1 = new FutureTask<>(new PrintNumber3Task(printNumber3, 1));
        FutureTask<Object> futureTask2 = new FutureTask<>(new PrintNumber3Task(printNumber3, 2));
        FutureTask<Object> futureTask3 = new FutureTask<>(new PrintNumber3Task(printNumber3, 3));
        Thread thread1 = new Thread(futureTask1, "AA");
        Thread thread2 = new Thread(futureTask2, "BB");
        Thread thread3 = new Thread(futureTask3, "CC");
        thread1.start();
        thread2.start();
        thread3.start();
        System.out.println("主线程等待异步任务完成......");
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("异步任务执行完毕，结束主线程");
    }
}

class PrintNumber3 {
    private int flag = 1;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition1 = lock.newCondition();
    private final Condition condition2 = lock.newCondition();
    private final Condition condition3 = lock.newCondition();

    public void print5(int loop) {
        lock.lock();
        try {
            while (flag != 1) {
                System.out.printf("[%s] 线程不满足flag条件，进入阻塞状态......%n", Thread.currentThread().getName());
                try {
                    condition1.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for (int i = 1; i <= 5; i++) {
                System.out.printf("[%s] 线程第 [%d] 进行打印，当前轮次为 [%d]%n", Thread.currentThread().getName(), i, loop);
            }
            flag = 2;
            condition2.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void print10(int loop) {
        lock.lock();
        try {
            while (flag != 2) {
                System.out.printf("[%s] 线程不满足flag条件，进入阻塞状态......%n", Thread.currentThread().getName());
                try {
                    condition2.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for (int i = 1; i <= 10; i++) {
                System.out.printf("[%s] 线程第 [%d] 进行打印，当前轮次为 [%d]%n", Thread.currentThread().getName(), i, loop);
            }
            flag = 3;
            condition3.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void print15(int loop) {
        lock.lock();
        try {
            while (flag != 3) {
                System.out.printf("[%s] 线程不满足flag条件，进入阻塞状态......%n", Thread.currentThread().getName());
                try {
                    condition3.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for (int i = 1; i <= 15; i++) {
                System.out.printf("[%s] 线程第 [%d] 进行打印，当前轮次为 [%d]%n", Thread.currentThread().getName(), i, loop);
            }
            flag = 1;
            condition1.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class PrintNumber3Task implements Callable<Object> {

    private final PrintNumber3 printNumber3;
    private final int flag;

    public PrintNumber3Task(PrintNumber3 printNumber3, int flag) {
        this.printNumber3 = printNumber3;
        this.flag = flag;
    }

    @Override
    public Object call() throws Exception {
        for (int i = 1; i <= 10; i++) {
            if (flag == 1) {
                printNumber3.print5(i);
            } else if (flag == 2) {
                printNumber3.print10(i);
            } else if (flag == 3) {
                printNumber3.print15(i);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return null;
    }

}