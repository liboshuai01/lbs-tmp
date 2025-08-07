package com.liboshuai.demo.thread.safe.collection;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetDemo {
    public static void main(String[] args) {
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(30);
        MySet mySet = new MySet();

        CompletableFuture<?>[] futures = new CompletableFuture[30];
        for (int i = 0; i < 30; i++) {
            futures[i] = CompletableFuture.runAsync(new UpdateSetTask(mySet), newFixedThreadPool);
        }

        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            System.out.println("\n在并发操作中发生了异常: " + e);
        } finally {
            // 关闭线程池
            newFixedThreadPool.shutdown();
            System.out.println("\n--- 所有线程执行完毕，关闭线程池 ---");
        }
    }
}

class MySet {
    private final Set<String> list = new CopyOnWriteArraySet<>();
    
    public void update(String data) {
        list.add(data);
        System.out.printf("[%s] 线程添加了一个元素，现在 set 集合存在的元素都有: [%s]%n", Thread.currentThread().getName(), list);
    }
}

class UpdateSetTask implements Runnable {

    private final MySet mySet;

    public UpdateSetTask(MySet mySet) {
        this.mySet = mySet;
    }

    @Override
    public void run() {
        mySet.update(UUID.randomUUID().toString().substring(0,4));
    }

}