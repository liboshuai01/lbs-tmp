package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueueTest {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建一个只有 1 个容量的队列
        MyBlockingQueue<String> queue = new MyBlockingQueue<>(1);

        // 2. 启动一个生产者线程 (尝试放入 3 个元素)
        new Thread(() -> {
            queue.put("任务1");
            queue.put("任务2"); // 此时应该会被阻塞，因为容量只有1
            queue.put("任务3");
            log.info("生产者任务全部投放完毕");
        }, "Producer").start();

        // 3. 主线程睡眠 2 秒，让生产者先跑，确保它进入"阻塞"状态
        Thread.sleep(2000);
        log.info("=== 消费者准备开始消费 ===");

        // 4. 消费者开始消费
        queue.take(); // 消费任务1 -> 应该唤醒生产者放入任务2
        Thread.sleep(1000);
        queue.take(); // 消费任务2 -> 应该唤醒生产者放入任务3
        queue.take(); // 消费任务3
    }
}