package com.liboshuai.demo.locksupport;

import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo2 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 开始执行...");
            // 在 park 之前，先休眠3秒，确保主线程的 unpark 已经执行
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName() + " 准备park...");
            // 因为主线程已经提前unpark，所以 t1 已经有了一个许可证
            // 调用park()会立刻消费掉这个许可证，然后立即返回，不会阻塞
            LockSupport.park();
            System.out.println(Thread.currentThread().getName() + " park执行完毕，没有阻塞。");
        }, "线程T1");
        t1.start();

        // 主线程休眠1秒，然后提前unpark
        Thread.sleep(1000);
        System.out.println("主线程提前unpark T1...");
        LockSupport.unpark(t1);
    }
}
