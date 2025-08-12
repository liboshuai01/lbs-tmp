package com.liboshuai.demo.threadlocal;

import java.util.UUID;

public class ThreadLocalBasicExample {

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            String value = UUID.randomUUID().toString();
            threadLocal.set(value);
            System.out.println(Thread.currentThread().getName() + ": 设置了自己的 threadlocal 值 " + value);
            System.out.println(Thread.currentThread().getName() + ": 获取了自己的 threadlocal 值 " + threadLocal.get());
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
