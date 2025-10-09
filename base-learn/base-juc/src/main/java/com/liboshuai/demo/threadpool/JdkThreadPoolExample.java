package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JdkThreadPoolExample {
    public static void main(String[] args) {
        AtomicInteger taskCount = new AtomicInteger();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(6),
                new CustomThreadFactory("lbs"),
                new CustomRejectedExecutionHandler()
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

    static class CustomThreadFactory implements ThreadFactory {

        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = namePrefix + threadNumber.getAndIncrement();
            Thread thread = new Thread(r, threadName);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            log.info("创建了线程[{}]", threadName);
            return thread;
        }
    }

    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("任务[{}]被拒绝", r);
        }
    }
}
