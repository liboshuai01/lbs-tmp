package com.liboshuai.demo.create;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CreateThread4 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        threadPool.execute(new R4());
        threadPool.submit(new C4());
        // 1. 发起关闭指令，拒绝新任务
        threadPool.shutdown();
        try {
            // 2. 等待一段时间，让已提交的任务执行完成
            if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                // 3. 如果超时，则强制关闭
                threadPool.shutdownNow();
                // 4. 再次等待，以便响应被中断的任务
                if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                    // 在此可以记录日志，表明线程池未能按时关闭
                    System.err.println("线程池未能按时关闭...");
                }
            }
        } catch (InterruptedException ie) {
            // 5. 如果当前线程在等待过程中被中断，也尝试强制关闭，并重置中断状态
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

class R4 implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                System.out.printf("线程 [%s] 偶数: %d%n", Thread.currentThread().getName(), i);
            }
        }
    }
}

class C4 implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 1) {
                System.out.printf("线程 [%s] 奇数: %d%n", Thread.currentThread().getName(), i);
            }
        }
        return null;
    }
}