package com.liboshuai.demo.thread.safe.lock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadDemo5 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Function5 function5 = new Function5();
        CompletableFuture<?>[] futures = new CompletableFuture[10];
        for (int i = 0; i < 10; i++) {
            futures[i] = CompletableFuture.runAsync(new Task5(function5), threadPool);
        }
        CompletableFuture.allOf(futures).join();
    }
}

class Task5 implements Runnable {

    private final Function5 function5;

    public Task5(Function5 function5) {
        this.function5 = function5;
    }

    @Override
    public void run() {
        function5.outMethod();
    }
}

class Function5 {

    private final ReentrantLock LOCK = new ReentrantLock();

    public void outMethod() {
        LOCK.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 进入外部方法");
            inMethod();
        } finally {
            LOCK.unlock();
        }
    }

    private void inMethod() {
        LOCK.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 进入内部方法");
        } finally {
            LOCK.unlock();
        }
    }
}
