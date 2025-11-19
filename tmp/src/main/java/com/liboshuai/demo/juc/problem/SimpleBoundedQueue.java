package com.liboshuai.demo.juc.problem;


import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 这是一个简化的有界阻塞队列，模拟网络缓冲池
 * 考察点：Wait/Notify 机制，虚假唤醒 (Spurious Wakeup)，条件队列
 */
public class SimpleBoundedQueue<T> {

    private final LinkedList<T> buffer = new LinkedList<>();
    private final int maxSize;
    private final ReentrantLock lock = new ReentrantLock(); // TODO: 将Object修改为ReentrantLock锁

    private final Condition notEmpty = lock.newCondition(); // TODO: 添加条件队列 notEmpty

    private final Condition notFull = lock.newCondition(); // TODO: 添加条件队列 notFull

    public SimpleBoundedQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 生产者：添加数据
     * 如果满了，应该阻塞
     */
    public void put(T data) throws InterruptedException {
        lock.lock();
        try {
            // -------------------------------------------------------
            // TODO: BUG 1 - 这里的判断逻辑有问题
            // -------------------------------------------------------
            while (buffer.size() == maxSize) {
                System.out.println(Thread.currentThread().getName() + " [PUT] 队列已满，等待中...");
                notFull.await(); // TODO: 进行await
            }

            buffer.add(data);
            System.out.println(Thread.currentThread().getName() + " [PUT] 生产了一个数据, 当前大小: " + buffer.size());

            // -------------------------------------------------------
            // TODO: BUG 2 - 这里的唤醒逻辑效率极低，甚至可能导致死锁般的“假死”
            // -------------------------------------------------------
            notEmpty.signal(); // TODO: 帮我分析为什么使用signal()比signAll更好
        } finally {
            lock.unlock();
        }
    }

    /**
     * 消费者：获取数据
     * 如果空了，应该阻塞
     */
    public T take() throws InterruptedException {
        lock.lock();
        try {
            // -------------------------------------------------------
            // TODO: BUG 1 - 这里的判断逻辑有问题
            // -------------------------------------------------------
            while (buffer.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + " [TAKE] 队列为空，等待中...");
                notEmpty.await(); // TODO: 进行await
            }

            T data = buffer.removeFirst();
            System.out.println(Thread.currentThread().getName() + " [TAKE] 消费了一个数据, 当前大小: " + buffer.size());

            // -------------------------------------------------------
            // TODO: BUG 2 - 这里的唤醒逻辑效率极低
            // -------------------------------------------------------
            notFull.signal();  // TODO: 帮我分析为什么使用signal()比signAll更好

            return data;
        } finally {
            lock.unlock();
        }
    }

    // -------------------------------------------------------
    // 测试入口
    // -------------------------------------------------------
    public static void main(String[] args) {
        SimpleBoundedQueue<Integer> queue = new SimpleBoundedQueue<>(2);

        // 启动 2 个生产者
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        queue.put(1);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) { e.printStackTrace(); }
            }, "Producer-" + i).start();
        }

        // 启动 2 个消费者
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        queue.take();
                        Thread.sleep(200); // 消费慢一点，容易触发队列满的情况
                    }
                } catch (InterruptedException e) { e.printStackTrace(); }
            }, "Consumer-" + i).start();
        }
    }
}