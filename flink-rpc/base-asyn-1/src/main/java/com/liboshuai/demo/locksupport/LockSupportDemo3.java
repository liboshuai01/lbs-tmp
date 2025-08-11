package com.liboshuai.demo.locksupport;

import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo3 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行，准备park...");
            LockSupport.park(); // 阻塞在这里

            // 当线程被中断后，park会返回，然后我们检查中断状态
            System.out.println(Thread.currentThread().getName() + " park返回了。");
            System.out.println(Thread.currentThread().getName() + " 的中断状态是：" + Thread.currentThread().isInterrupted());
        }, "线程T1");
        t1.start();

        // 等待2秒，确保T1已经park
        Thread.sleep(2000);

        System.out.println("主线程准备中断 T1...");
        t1.interrupt(); // 中断t1
    }
}
