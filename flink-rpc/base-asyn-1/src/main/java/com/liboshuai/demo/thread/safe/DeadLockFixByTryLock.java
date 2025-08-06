package com.liboshuai.demo.thread.safe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadLockFixByTryLock {
    public static void main(String[] args) throws InterruptedException {
        // s1 和 s2 是共享资源
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();

        // 为每个共享资源创建独立的锁对象
        Lock lock1 = new ReentrantLock();
        Lock lock2 = new ReentrantLock();

        // 启动线程
        Thread t1 = new Thread(new Task(s1, s2, lock1, lock2, true));
        Thread t2 = new Thread(new Task(s1, s2, lock1, lock2, false));
        t1.start();
        t2.start();

        System.out.println("主线程等待异步执行完毕");
        t1.join();
        t2.join();
        System.out.println("异步执行完毕啦！");
    }
}

class Task implements Runnable {
    private final StringBuilder s1;
    private final StringBuilder s2;
    private final Lock lock1;
    private final Lock lock2;
    private final boolean isTask1; // 用于区分任务逻辑

    public Task(StringBuilder s1, StringBuilder s2, Lock lock1, Lock lock2, boolean isTask1) {
        this.s1 = s1;
        this.s2 = s2;
        this.lock1 = lock1;
        this.lock2 = lock2;
        this.isTask1 = isTask1;
    }

    @Override
    public void run() {
        if (isTask1) {
            runLogic("a", "1", "b", "2", lock1, lock2);
        } else {
            // 模拟原死锁场景的逆序加锁
            runLogic("c", "3", "d", "4", lock2, lock1);
        }
    }

    private void runLogic(String c1, String n1, String c2, String n2, Lock firstLock, Lock secondLock) {
        String threadName = Thread.currentThread().getName();
        while (true) { // 持续尝试直到成功
            // 尝试获取第一个锁
            if (firstLock.tryLock()) {
                System.out.println(threadName + ": locked first lock");
                try {
                    // 尝试获取第二个锁
                    if (secondLock.tryLock()) {
                        System.out.println(threadName + ": locked second lock");
                        try {
                            // *** 成功获取所有锁，执行业务逻辑 ***
                            s1.append(c1);
                            s2.append(n1);

                            TimeUnit.MILLISECONDS.sleep(100); // 模拟操作耗时

                            s1.append(c2);
                            s2.append(n2);

                            System.out.println(threadName + " final s1: " + s1);
                            System.out.println(threadName + " final s2: " + s2);

                            // 任务完成，跳出循环
                            break;
                        } finally {
                            // 必须在 finally 块中释放锁
                            secondLock.unlock();
                            System.out.println(threadName + ": unlocked second lock");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //
                } finally {
                    // 必须在 finally 块中释放锁
                    firstLock.unlock();
                    System.out.println(threadName + ": unlocked first lock");
                }
            }
            // 如果未能获取全部锁，短暂休眠后重试，避免CPU空转
            try {
                System.out.println(threadName + ": failed to get all locks, retrying...");
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}