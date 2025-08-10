package com.liboshuai.demo.safe.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureTaskTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            int sum = 0;
            for (int i = 0; i < 10000; i++) {
                if (i % 2 == 0) {
                    sum += i;
                }
            }
            return sum;
        });
        new Thread(futureTask, "线程1").start();
        System.out.println("主线程继续做自己的计算......");
        int sum = 0;
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 1) {
                sum += i;
            }
        }
        while (!futureTask.isDone()) {
            System.out.println("主线程等待异步计算结果......");
            TimeUnit.MILLISECONDS.sleep(10);
        }
        Integer result = futureTask.get();
        int finalResult = result + sum;
        System.out.println("主线程获取的异步计算做汇总: " + finalResult);
    }
}
