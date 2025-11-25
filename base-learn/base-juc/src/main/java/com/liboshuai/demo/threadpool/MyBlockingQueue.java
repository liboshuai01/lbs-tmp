package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 ReentrantLock 实现的阻塞队列
 * @param <T> 任务类型
 */
@Slf4j
public class MyBlockingQueue<T> {

    // 1. 任务容器
    private final Deque<T> queue = new ArrayDeque<>();

    // 2. 容量限制
    private final int capacity;

    // 3. 锁
    private final ReentrantLock lock = new ReentrantLock();

    // 4. 条件变量
    // 消费者等待区 (队列空时，去这里等)
    private final Condition emptyWaitSet = lock.newCondition();
    // 生产者等待区 (队列满时，去这里等)
    private final Condition fullWaitSet = lock.newCondition();

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 阻塞获取 (消费者)
     */
    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    log.info("队列为空，消费者线程 [{}] 进入等待...", Thread.currentThread().getName());
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    // 恢复中断状态并返回null，交由上层处理
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            T t = queue.removeFirst();
            log.info("消费者线程 [{}] 获取任务: {}", Thread.currentThread().getName(), t);

            // 唤醒生产者
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带超时的阻塞获取 (用于非核心线程回收)
     */
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    if (nanos <= 0) {
                        return null; // 超时返回
                    }
                    // awaitNanos 返回剩余需要等待的时间
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞添加 (生产者)
     */
    public void put(T task) {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    log.info("队列已满，生产者线程 [{}] 进入等待...", Thread.currentThread().getName());
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            queue.addLast(task);
            log.info("生产者线程 [{}] 加入任务: {}", Thread.currentThread().getName(), task);

            // 唤醒消费者
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带超时的添加 (用于拒绝策略)
     */
    public boolean offer(T task, long timeout, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                try {
                    if (nanos <= 0) {
                        log.warn("等待超时，任务加入失败: {}", task);
                        return false;
                    }
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            queue.addLast(task);
            log.info("生产者线程 [{}] 加入任务: {}", Thread.currentThread().getName(), task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}