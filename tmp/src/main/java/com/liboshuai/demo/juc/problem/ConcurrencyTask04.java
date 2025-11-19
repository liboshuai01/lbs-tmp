package com.liboshuai.demo.juc.problem;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 第四关：ReentrantLock 与 Condition 实现精准唤醒
 * 优化生产者-消费者模型，避免惊群效应
 */
public class ConcurrencyTask04 {

    public static void main(String[] args) {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(2);

        // 启动 3 个生产者
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                int val = 0;
                while (true) {
                    try {
                        buffer.put(++val);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Producer-" + i).start();
        }

        // 启动 3 个消费者
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        buffer.take();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Consumer-" + i).start();
        }
    }

    // --- 基于 Lock 和 Condition 的缓冲区 ---
    static class BoundedBuffer<T> {
        private final Queue<T> queue = new LinkedList<>();
        private final int capacity;

        // TODO: 定义锁和两个条件变量
        private final Lock lock = new ReentrantLock();
        // private final Condition ...
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        public BoundedBuffer(int capacity) {
            this.capacity = capacity;
            // TODO: 初始化 Condition
        }

        public void put(T data) throws InterruptedException {
            // TODO: 任务 A - 使用 Lock 和 Condition 实现 put
            // 1. 加锁
            // 2. while(满) -> notFull.await()
            // 3. 入队
            // 4. notEmpty.signal() (注意：可以用 signal 了，不用 signalAll)
            // 5. 解锁
            lock.lock();
            try {
                while (queue.size() == capacity) {
                    notFull.await();
                }
                queue.add(data);
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public T take() throws InterruptedException {
            // TODO: 任务 A - 使用 Lock 和 Condition 实现 take
            T data;
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    notEmpty.await();
                }
                data = queue.remove();
                notFull.signal();
            } finally {
                lock.unlock();
            }
            return data;
        }
    }

    /*
     * TODO: 思考题
     * 调用 condition.await() 前必须持有 lock 吗？为什么？
     * 答：必须持有, 因为condition.await()和condition.signal()/signalAll()都是必须在锁内才可以使用. 如果不想在锁内使用, 可以使用LockSupport.park()和unpark()方法.
     */
}
