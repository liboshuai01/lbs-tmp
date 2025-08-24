package com.liboshuai.demo.queue;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LbsBlockingQueue 接口的一个基于链表的简易实现。
 * 此实现使用了"两把锁"（读写分离）的并发策略，与 java.util.concurrent.LinkedBlockingQueue 类似，
 * 使得生产者和消费者可以最大程度地并发执行。
 *
 * @param <E> 队列中元素的类型
 */
public class LbsLinkedBlockingQueue<E> implements LbsBlockingQueue<E> {

    /**
     * 链表节点类
     */
    static class Node<E> {
        E item;
        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

    /**
     * 队列容量
     */
    private final int capacity;

    /**
     * 当前队列中的元素数量，使用 AtomicInteger 保证其在两个锁之间的可见性和原子性
     */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * 链表头指针，head.item 永远为 null（哨兵节点）
     */
    private transient Node<E> head;

    /**
     * 链表尾指针
     */
    private transient Node<E> last;

    /**
     * 生产者（put, offer, add）锁
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * 当队列满时，生产者在此条件上等待
     */
    private final Condition notFull = putLock.newCondition();

    /**
     * 消费者（take, poll, remove）锁
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * 当队列空时，消费者在此条件上等待
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * 默认构造函数，容量为 Integer.MAX_VALUE
     */
    public LbsLinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 构造一个指定容量的队列
     * @param capacity 队列的最大容量
     */
    public LbsLinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        // 初始化头尾节点为同一个哨兵节点
        last = head = new Node<>(null);
    }

    // --- 辅助方法 ---

    /**
     * 入队操作，将新节点链接到链表尾部。
     * 调用此方法前必须获取 putLock。
     */
    private void enqueue(Node<E> node) {
        last.next = node;
        last = node;
    }

    /**
     * 出队操作，返回并移除旧的头节点。
     * 调用此方法前必须获取 takeLock。
     */
    private E dequeue() {
        Node<E> h = head;
        Node<E> first = h.next;
        h.next = h; // help GC
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * 唤醒一个可能在等待的消费者。
     * 当一个元素被成功添加后调用。
     */
    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * 唤醒一个可能在等待的生产者。
     * 当一个元素被成功取出后调用。
     */
    private void signalNotFull() {
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }


    // --- 实现接口方法 ---

    @Override
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();

        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;

        putLock.lockInterruptibly();
        try {
            // 如果队列已满，则在 notFull 条件上阻塞等待
            while (count.get() == capacity) {
                notFull.await();
            }
            enqueue(new Node<>(e));
            c = count.getAndIncrement();
            // 如果队列在添加前是空的，那么现在不空了，需要唤醒一个消费者
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }

        // 如果之前的队列是空的，唤醒可能在等待的消费者
        if (c == 0) {
            signalNotEmpty();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;

        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {
                if (nanos <= 0) {
                    return false; // 超时
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(new Node<>(e));
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final AtomicInteger count = this.count;
        if (count.get() == capacity) {
            return false;
        }
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                enqueue(new Node<>(e));
                c = count.getAndIncrement();
                if (c + 1 < capacity) {
                    notFull.signal();
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return c >= 0;
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
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;

        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }

        // 如果队列在取出前是满的，现在不满了，需要唤醒一个生产者
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;

        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1) {
                    notEmpty.signal();
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
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
        if (count.get() == 0) {
            return null;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            // head是哨兵，第一个元素是head.next
            return (head.next != null) ? head.next.item : null;
        } finally {
            takeLock.unlock();
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

        int n = 0;
        // 为了安全地排空队列，我们需要同时持有两把锁
        putLock.lock();
        takeLock.lock();
        try {
            Node<E> h = head;
            while(h.next != null) {
                h = h.next;
                c.add(h.item);
                h.item = null; // help GC
                n++;
            }

            if (n > 0) {
                // 重置队列为空状态
                head = last;
                if (count.getAndSet(0) == capacity) {
                    // 如果之前是满的，现在空了，唤醒所有生产者
                    notFull.signalAll();
                }
            }
            return n;
        } finally {
            takeLock.unlock();
            putLock.unlock();
        }
    }
}