package com.liboshuai.demo.threadlocal;

public class InheritableThreadLocalExample {
    public static void main(String[] args) throws InterruptedException {
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

        inheritableThreadLocal.set("main值");
        System.out.println("在父线程中设置threadlocal值: " + inheritableThreadLocal.get());

        Thread t1 = new Thread(() -> {
            String oldValue = inheritableThreadLocal.get();
            System.out.println("子线程中获取threadlocal值: " + oldValue);
            inheritableThreadLocal.set("子线程值");
            System.out.println("子线程中设置threadlocal值: " + inheritableThreadLocal.get());
        });
        t1.start();
        t1.join();
        System.out.println("在父线程中重新获取threadlocal的值: " + inheritableThreadLocal.get());
    }
}
