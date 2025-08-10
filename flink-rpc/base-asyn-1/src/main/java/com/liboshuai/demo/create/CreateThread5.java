package com.liboshuai.demo.create;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CreateThread5 {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            int sum = 0;
            for (int i = 0; i < 100; i++) {
                if (i % 2 == 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(i);
                    sum += i;
                }
            }
            return sum;
        }, threadPool);
        // 注册异步回调函数，当任务完成，自动调用这里的代码
        completableFuture.thenAccept(result -> System.out.println("最终计算结果为:" + result));
        // 注册异常处理回调函数，当任务异常，自动调用这里的代码
        completableFuture.exceptionally(throwable -> {
            System.out.println("计算异常：" + throwable.getMessage());
            return null; // 返回默认值null
        });
        // 主线程可以继续做其他事情，完全不需要关系异步任务什么时候结束
        System.out.println("主线程：任务已提交，继续做自己的事情......");
        for (int i = 0; i < 10; i++) {
            System.out.println("主线程正在执行其他任务... " + i);
            TimeUnit.MILLISECONDS.sleep(300);
        }
        // 异步任务结束后，关闭线程池
        completableFuture.join();
        threadPool.shutdown();
        System.out.println("主线程：任务执行完毕，线程池已关闭......");
    }
}
