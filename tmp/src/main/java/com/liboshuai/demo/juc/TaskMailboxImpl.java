package com.liboshuai.demo.juc;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskMailboxImpl implements TaskMailbox {

    // 使用 LinkedList 模拟队列 (Flink 源码使用 RingBuffer 优化性能)
    private final LinkedList<Runnable> queue = new LinkedList<>();

    // JUC 锁机制
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private volatile boolean isQuiesced = false; // 是否停止接收

    @Override
    public boolean hasMail() {
        lock.lock();
        try {
            return !queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(Runnable mail) {
        lock.lock();
        try {
            if (isQuiesced) {
                return;
            }
            queue.add(mail);
            // 唤醒阻塞在 take() 的主线程
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Runnable tryTake(int priority) {
        lock.lock();
        try {
            return queue.pollFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Runnable take(int priority) throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                // 阻塞等待，直到有信件放入
                notEmpty.await();
            }
            return queue.pollFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Runnable> drain() {
        lock.lock();
        try {
            List<Runnable> remaining = new LinkedList<>(queue);
            queue.clear();
            return remaining;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void quiesce() {
        lock.lock();
        try {
            isQuiesced = true;
        } finally {
            lock.unlock();
        }
    }
}
