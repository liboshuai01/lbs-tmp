package com.liboshuai.demo.juc.problem;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * 第三关：等待通知机制与虚假唤醒
 * 模拟 Flink 网络缓冲区的生产者-消费者模型
 */
public class ConcurrencyTask03 {

    public static void main(String[] args) {
        // 创建一个容量为 2 的缓冲区
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(2);

        // 启动 2 个生产者
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                int val = 0;
                while (true) {
                    try {
                        buffer.put(++val);
                        // 模拟生产耗时
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Producer-" + i).start();
        }

        // 启动 2 个消费者
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        buffer.take();
                        // 模拟消费耗时
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Consumer-" + i).start();
        }
    }

    private static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { }
    }

    // --- 待实现的缓冲区 ---
    static class BoundedBuffer<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;
        private int count = 0;

        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        /**
         * 生产者写入数据
         * 如果满了，阻塞等待；写入后，通知消费者。
         */
        public synchronized void put(T data) throws InterruptedException {
            // TODO: 任务 A - 实现 put 逻辑
            // 1. 判断队列是否已满 (注意：用 if 还是 while?)
            // 2. 满了就 wait
            // 3. 没满就入队
            // 4. 唤醒其他线程

            // --- 你的代码开始 ---
            while (queue.size() == capacity) {
                this.wait();
            }
            queue.add(data);
            this.notifyAll();
            // --- 你的代码结束 ---

            System.out.println(Thread.currentThread().getName() + " 生产: " + data + " [当前数量:" + count + "]");
        }

        /**
         * 消费者读取数据
         * 如果空了，阻塞等待；读取后，通知生产者。
         */
        public synchronized T take() throws InterruptedException {
            T data = null;
            // TODO: 任务 A - 实现 take 逻辑
            // 1. 判断队列是否为空
            // 2. 空了就 wait
            // 3. 不空就出队
            // 4. 唤醒其他线程

            // --- 你的代码开始 ---
            while(queue.isEmpty()) {
                this.wait();
            }
            data = queue.remove();
            this.notifyAll();
            // --- 你的代码结束 ---

            System.out.println(Thread.currentThread().getName() + " 消费: " + data + " [当前数量:" + count + "]");
            return data;
        }
    }

    /*
     * TODO: 思考题回答
     * 1. 为什么 wait 判断必须用 while 而不是 if？
     * 答：防止虚假唤醒
     * * 2. 为什么建议用 notifyAll 而不是 notify？如果这里用 notify 会怎样？
     * 答：notify可能会出现信号丢死或者假死的问题
     */
}
