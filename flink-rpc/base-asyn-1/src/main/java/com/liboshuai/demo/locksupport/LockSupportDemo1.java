package com.liboshuai.demo.locksupport;

import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo1 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行，准备park...");
            // 此时没有许可证，调用 park() 会阻塞
            LockSupport.park();
            System.out.println(Thread.currentThread().getName() + " 被唤醒，继续执行。");
        }, "线程1");
        t1.start();

        // 让主线程休眠2秒，确保t1已经执行到park()并被阻塞
        Thread.sleep(2000);

        System.out.println("主线程准备unpark T1...");
        // 主线程为t1颁发许可证
        LockSupport.unpark(t1);
    }
}
