package com.liboshuai.demo.threadlocal;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TtlExample {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<String>();

        threadLocal.set("主线程1");
        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] 获取到值 [%s]%n", threadName, threadLocal.get());
        };
        TtlRunnable ttlRunnable = TtlRunnable.get(task);
        threadPool.submit(ttlRunnable);
        TimeUnit.SECONDS.sleep(1);
        System.out.println(">>>> 对比 <<<");
        threadLocal.set("主线程2");
        threadPool.submit(ttlRunnable);
    }
}