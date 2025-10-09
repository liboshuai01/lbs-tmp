package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MyThreadPoolExample {
    public static void main(String[] args) {
        AtomicInteger taskCount = new AtomicInteger();
        MyThreadPoolExecutor threadPoolExecutor = new MyThreadPoolExecutor(
                2,
                4
        );
        // 日志打印：创建10个线程，10个提交成功，两个被拒绝
        for (int i = 0; i < 12; i++) {
            threadPoolExecutor.execute(() -> {
                int count = taskCount.getAndIncrement();
                log.info("提交了任务{}", count);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("任务{}结束了", count);
            });
        }
        log.info("主线程继续执行");
        threadPoolExecutor.shutdown();
    }
}
