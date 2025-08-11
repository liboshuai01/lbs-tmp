package com.liboshuai.demo.locksupport;

import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo4 {
    public static void main(String[] args) throws InterruptedException {
        final String blockerInfo = "This is a blocker for demo 4";

        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行，准备带blocker的park...");
            // 使用带有blocker的park方法
            LockSupport.park(blockerInfo);
            System.out.println(Thread.currentThread().getName() + " 被唤醒。");
        }, "线程T1");
        t1.start();

        // 休眠1秒，让t1有时间park
        Thread.sleep(1000);

        // 在另一个线程中查看t1的blocker信息
        System.out.println("主线程查看T1的Blocker信息：" + LockSupport.getBlocker(t1));

        // 休眠2秒后唤醒t1
        Thread.sleep(2000);
        System.out.println("主线程准备unpark T1...");
        LockSupport.unpark(t1);

        // t1被唤醒后，blocker信息会消失
        // 等待t1执行完毕
        t1.join();
        System.out.println("T1执行完毕后，Blocker信息：" + LockSupport.getBlocker(t1));
    }
}
