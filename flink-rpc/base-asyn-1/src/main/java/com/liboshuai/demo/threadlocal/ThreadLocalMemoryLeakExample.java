package com.liboshuai.demo.threadlocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadLocalMemoryLeakExample {
    public static void main(String[] args) throws InterruptedException {
        final ExecutorService threadPool = Executors.newSingleThreadExecutor();
        final ThreadLocal<byte[]> threadLocal = new ThreadLocal<>();
        for (int i = 0; i < 3; i++) {
            threadPool.execute(() -> {
                String threadName = Thread.currentThread().getName();
                byte[] old_bytes = threadLocal.get();
                System.out.printf("[%s] 获取到旧值 [%s]%n", threadName, old_bytes);
                byte[] new_bytes = new byte[1024 * 1024];
                threadLocal.set(new_bytes);
                System.out.printf("[%s] 设置了新值 [%s]%n", threadName, new_bytes);
            });
            TimeUnit.SECONDS.sleep(1);
        }
        threadPool.shutdown();
    }
}