package com.liboshuai.demo.queue;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LbsBlockingQueue 接口的一个基于数组的简易实现。
 * 此实现是一个有界的阻塞队列，其大小在构造时确定。
 * 它内部使用一个 ReentrantLock 和两个 Condition 来管理并发。
 *
 * @param <E> 队列中元素的类型
 */
public class LbsArrayBlockingQueue<E> implements LbsBlockingQueue<E> {

    /**
     * 存储元素的数组
     */
    private final E[] items;

    /**
     * 下一次 take, poll, peek 或 remove 操作的数组索引
     */
    private int takeIndex;

    /**
     * 下一次 put, offer, or add 操作的数组索引
     */
    private int putIndex;

    /**
     * 队列中的元素数量
     */
    private int count;

    /**
     * 控制所有访问的主锁
     */
    private final ReentrantLock lock;

    /**
     * 当队列为空时，消费者在此条件上等待
     */
    private final Condition notEmpty;

    /**
     * 当队列满时，生产者在此条件上等待
     */
    private final Condition notFull;

    /**
     * 构造一个指定容量和公平策略的队列。
     * @param capacity 队列的容量
     * @param fair     如果为 true，则锁和条件使用公平策略
     */
    @SuppressWarnings("unchecked")
    public LbsArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = (E[]) new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    /**
     * 构造一个指定容量、使用默认（非公平）策略的队列。
     * @param capacity 队列的容量
     */
    public LbsArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    // --- 辅助方法 ---

    /**
     * 将元素插入数组尾部。
     * 调用此方法前必须已获取锁。
     */
    private void enqueue(E x) {
        items[putIndex] = x;
        if (++putIndex == items.length) {
            putIndex = 0; // 循环数组
        }
        count++;
        // 插入元素后，队列肯定不为空，唤醒一个消费者
        notEmpty.signal();
    }

    /**
     * 从数组头部移除元素。
     * 调用此方法前必须已获取锁。
     */
    private E dequeue() {
        E x = items[takeIndex];
        items[takeIndex] = null; // 帮助 GC
        if (++takeIndex == items.length) {
            takeIndex = 0; // 循环数组
        }
        count--;
        // 取出元素后，队列肯定未满，唤醒一个生产者
        notFull.signal();
        return x;
    }

    // --- 实现接口方法 ---

    @Override
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                notFull.await();
            }
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0) {
                    return false; // 超时
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count == items.length) {
                return false;
            } else {
                enqueue(e);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        if (offer(e)) {
            return true;
        } else {
            throw new IllegalStateException("Queue full");
        }
    }

    @Override
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (count == 0) ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E remove() {
        E x = poll();
        if (x != null) {
            return x;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (count == 0) ? null : items[takeIndex];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E element() {
        E x = peek();
        if (x != null) {
            return x;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        if (c == null) throw new NullPointerException();
        if (c == this) throw new IllegalArgumentException();

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            int max = count;
            while(n < max){
                c.add(dequeue());
                n++;
            }
            // 在 ArrayBlockingQueue 的模型中，dequeue 内部已经 signal 了
            // 为了确保唤醒足够多的生产者，这里可以改为 signalAll
            if (n > 0) {
                notFull.signalAll();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }
}