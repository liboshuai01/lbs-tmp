package com.liboshuai.demo.thread.readwritelock;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 示例 6: 演示写锁如何与 Condition 配合使用。
 * 场景：一个有界队列，生产者和消费者都需要独占访问（因为它们都修改队列），
 * 所以都使用写锁。当队列满或空时，使用 Condition 进行等待和通知。
 */
public class BoundedBufferWithCondition {
    private final Queue<String> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    private final Condition notFull = writeLock.newCondition(); // 队列未满的条件
    private final Condition notEmpty = writeLock.newCondition(); // 队列非空的条件

    public BoundedBufferWithCondition(int capacity) {
        this.capacity = capacity;
    }

    public void produce(String item) throws InterruptedException {
        writeLock.lock();
        try {
            // 当队列满时，生产者等待
            while (queue.size() == capacity) {
                System.out.println(Thread.currentThread().getName() + " -> 队列已满，等待消费...");
                notFull.await(); // 释放写锁并等待
            }
            queue.add(item);
            System.out.println(Thread.currentThread().getName() + " -> 生产了: " + item);
            // 生产后，队列肯定不为空，唤醒可能在等待的消费者
            notEmpty.signalAll();
        } finally {
            writeLock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        writeLock.lock();
        try {
            // 当队列空时，消费者等待
            while (queue.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + " -> 队列已空，等待生产...");
                notEmpty.await(); // 释放写锁并等待
            }
            String item = queue.poll();
            System.out.println(Thread.currentThread().getName() + " -> 消费了: " + item);
            // 消费后，队列肯定未满，唤醒可能在等待的生产者
            notFull.signalAll();
            return item;
        } finally {
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        BoundedBufferWithCondition buffer = new BoundedBufferWithCondition(3);

        // 生产者线程
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    buffer.produce("Item-" + i);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer").start();

        // 消费者线程
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    buffer.consume();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer").start();
    }
}