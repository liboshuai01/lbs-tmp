package com.liboshuai.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Test04 {
    public static void main(String[] args) throws InterruptedException {
        Instant start = Instant.now();
        Thread t1 = new Thread(new TaskA());
        Thread t2 = new Thread(new TaskB(t1));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Instant end = Instant.now();
        System.out.println("耗时：" + Duration.between(start, end).toMillis()); // 预期7秒多
    }

    /**
     * 1. 洗水壶
     * 2. 烧开水
     */
    static class TaskA implements Runnable {

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("洗水壶完成");
                TimeUnit.SECONDS.sleep(5);
                System.out.println("烧开水完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 1. 洗茶壶
     * 2. 洗茶杯
     * 3. 拿茶叶
     * 4. 泡茶
     */
    static class TaskB implements Runnable{

        private final Thread thread;

        TaskB(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("洗茶壶完成");
                TimeUnit.SECONDS.sleep(1);
                System.out.println("洗茶杯完成");
                TimeUnit.SECONDS.sleep(1);
                System.out.println("拿茶叶完成");
                System.out.println("等待TaskA烧开水完成");
                thread.join();
                TimeUnit.SECONDS.sleep(1);
                System.out.println("泡茶完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
