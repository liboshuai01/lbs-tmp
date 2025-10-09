package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Main {
    public static void main(String[] args) {
        LbsThreadPoolExecutor lbsThreadpoolExecutor = new LbsThreadPoolExecutor(2, 4);
        for (int i = 0; i < 5; i++) {
            lbsThreadpoolExecutor.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println(Thread.currentThread().getName());
        }
        System.out.println("主线程没有被阻塞");
    }
}
