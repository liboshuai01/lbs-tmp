package com.liboshuai.demo.thread.safe.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadDemo4 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Function4 function4 = new Function4();
        for (int i = 0; i < 10; i++) {
            threadPool.execute(new Task4(function4));
        }
        threadPool.shutdown();
    }
}

class Function4 {

    public synchronized void outMethod() {
        System.out.println(Thread.currentThread().getName() + " 进入外部方法");
        inMethod();
    }

    private synchronized void inMethod() {
        System.out.println(Thread.currentThread().getName() + " 进入内部方法");
    }
}


class Task4 implements Runnable {

    private final Function4 function4;

    public Task4(Function4 function4) {
        this.function4 = function4;
    }

    @Override
    public void run() {
        function4.outMethod();
    }
}
