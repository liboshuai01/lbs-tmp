package com.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 ReentrantLock 和 Condition 实现的简易阻塞队列
 * 核心功能：
 * 1. 线程安全地入队和出队
 * 2. 队列满时，生产者阻塞；队列空时，消费者阻塞
 * 3. 支持带超时的阻塞操作
 *
 * @param <T> 任务类型
 */
@Slf4j
public class MyBlockingQueue<T> {

    // 任务容器 (双端队列)
    private final Deque<T> queue = new ArrayDeque<>();

    // 队列容量上限
    private final int capacity;

    // 独占锁 (保证并发安全)
    private final ReentrantLock lock = new ReentrantLock();

    // 消费者条件变量 (队列空了，去这里等 -> EmptyWaitSet)
    private final Condition emptyWaitSet = lock.newCondition();

    // 生产者条件变量 (队列满了，去这里等 -> FullWaitSet)
    private final Condition fullWaitSet = lock.newCondition();

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 阻塞获取任务 (一直等待直到拿到任务)
     * 用于核心线程的日常工作
     */
    public T take() throws InterruptedException {
        lock.lock();
        try {
            // while 防止虚假唤醒 (Spurious Wakeup)
            while (queue.isEmpty()) {
                // 队列空，消费者挂起
                emptyWaitSet.await();
            }
            // 拿任务
            T t = queue.removeFirst();
            // 唤醒生产者 (可能之前队列满，生产者在等)
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带超时的阻塞获取任务
     * 用于非核心线程 (或者允许超时的核心线程)
     * @param timeout 超时数值
     * @param unit 时间单位
     * @return 任务对象，如果超时则返回 null
     */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                if (nanos <= 0) {
                    // 超时时间到了，还是没任务，返回 null
                    return null;
                }
                // awaitNanos 返回剩余需要等待的时间
                nanos = emptyWaitSet.awaitNanos(nanos);
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞添加任务 (一直等待直到放入成功)
     */
    public void put(T task) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                // 队列满，生产者挂起
                fullWaitSet.await();
            }
            queue.addLast(task);
            // 唤醒消费者 (可能有消费者因为空队列在睡大觉)
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带超时的添加任务
     * 用于 execute 方法中尝试入队的操作，不希望一直死等
     * @return true: 添加成功; false: 队列满且超时
     */
    public boolean offer(T task, long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = fullWaitSet.awaitNanos(nanos);
            }
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前队列大小
     */
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}