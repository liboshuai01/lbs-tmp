package com.liboshuai.demo.queue;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LbsBlockingQueue 接口的一个 "同步" 队列的简易实现。
 * 此队列的容量为零，它不存储元素。相反，它作为一个线程间直接传递元素的机制。
 * 每个 put 操作都必须等待一个对应的 take 操作，反之亦然。
 *
 * 这是一个简化的、公平的实现。
 *
 * @param <E> 队列中元素的类型
 */
public class LbsSynchronousQueue<E> implements LbsBlockingQueue<E> {

    /**
     * 控制所有访问的主锁
     */
    private final ReentrantLock lock;

    /**
     * 用于生产者和消费者等待的条件变量
     */
    private final Condition condition;

    /**
     * 用于在生产者和消费者之间传递元素的 "交易槽"
     * 如果为 null，表示没有等待的生产者。
     * 如果非 null，表示有一个生产者正在等待它的元素被消费。
     */
    private E transferItem = null;

    /**
     * 构造一个具有指定公平策略的同步队列。
     * @param fair 如果为 true，则等待的线程以 FIFO 顺序竞争
     */
    public LbsSynchronousQueue(boolean fair) {
        this.lock = new ReentrantLock(fair);
        this.condition = lock.newCondition();
    }

    /**
     * 构造一个默认（非公平）的同步队列。
     */
    public LbsSynchronousQueue() {
        this(false);
    }

    @Override
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();

        lock.lockInterruptibly();
        try {
            // 等待，直到没有其他生产者在等待
            while (transferItem != null) {
                condition.await();
            }

            // 将元素放入交易槽
            transferItem = e;
            condition.signalAll(); // 唤醒可能在等待的消费者

            // 等待，直到消费者取走了元素
            while (transferItem != null) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        E item;
        lock.lockInterruptibly();
        try {
            // 等待，直到有生产者放入元素
            while (transferItem == null) {
                condition.await();
            }

            // 从交易槽中取出元素
            item = transferItem;
            transferItem = null;

            // 唤醒放入该元素的生产者，告知它元素已被消费
            condition.signalAll();
        } finally {
            lock.unlock();
        }
        return item;
    }


    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);

        lock.lockInterruptibly();
        try {
            // 等待，直到没有其他生产者在等待 (带超时)
            while (transferItem != null) {
                if (nanos <= 0) return false;
                nanos = condition.awaitNanos(nanos);
            }

            transferItem = e;
            condition.signalAll();

            // 等待消费者取走元素 (带超时)
            while (transferItem != null) {
                if (nanos <= 0) {
                    // 超时了，但消费者还没来，必须撤销我们的 offer
                    transferItem = null;
                    condition.signalAll(); // 唤醒其他可能在等待的生产者
                    return false;
                }
                nanos = condition.awaitNanos(nanos);
            }
            return true; // 成功交接
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        E item;

        lock.lockInterruptibly();
        try {
            // 等待生产者放入元素 (带超时)
            while (transferItem == null) {
                if (nanos <= 0) return null;
                nanos = condition.awaitNanos(nanos);
            }

            item = transferItem;
            transferItem = null;
            condition.signalAll();
            return item;
        } finally {
            lock.unlock();
        }
    }


    // --- 以下方法基于 SynchronousQueue 容量为零的特性实现 ---

    /**
     * 总是返回 false，因为 SynchronousQueue 没有容量，
     * 除非另一个线程已经在等待接收它，否则不能立即添加元素。
     */
    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        // 不能立即插入，因为没有容量
        return false;
    }

    /**
     * 总是抛出 IllegalStateException，因为队列容量为0，永远是“满”的。
     */
    @Override
    public boolean add(E e) {
        throw new IllegalStateException("Queue full");
    }

    /**
     * 总是返回 null，因为除非有线程在等待提供元素，否则队列中没有任何元素。
     */
    @Override
    public E poll() {
        return null;
    }

    /**
     * 总是抛出 NoSuchElementException，因为队列永远是“空”的。
     */
    @Override
    public E remove() {
        throw new NoSuchElementException();
    }

    /**
     * 总是返回 null，因为队列从不“包含”元素。
     */
    @Override
    public E peek() {
        return null;
    }

    /**
     * 总是抛出 NoSuchElementException，因为队列永远是“空”的。
     */
    @Override
    public E element() {
        throw new NoSuchElementException();
    }

    /**
     * 不做任何事并返回 0，因为队列中没有可排出的元素。
     */
    @Override
    public int drainTo(Collection<? super E> c) {
        if (c == null) throw new NullPointerException();
        if (c == this) throw new IllegalArgumentException();
        // 队列中没有元素可排出
        return 0;
    }
}