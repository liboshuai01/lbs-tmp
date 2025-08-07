package com.liboshuai.demo.thread.create;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 创建线程方式3：实现Callable接口
 */
public class CreateThread3 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Task3 task3 = new Task3();
        FutureTask<Integer> futureTask = new FutureTask<>(task3);
        Thread thread = new Thread(futureTask, "线程1");
        thread.start();
        System.out.println("主线程等待异步计算结果......");
        while (!futureTask.isDone()) {
            System.out.println("异步计算结果未完成，主线程可以做自己的事情......");
            TimeUnit.MILLISECONDS.sleep(10);
        }
        // 阻塞获取异步结束结果
        Integer result = futureTask.get();
        System.out.println("主线程获取的异步计算结果为: " + result);
    }
}

class Task3 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                System.out.println(i);
                sum += i;
            }          
            TimeUnit.MILLISECONDS.sleep(10);
        }
        return sum;
    }
}
