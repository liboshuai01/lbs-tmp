package com.liboshuai.demo.queue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 一个简易的阻塞队列接口，模仿 java.util.concurrent.BlockingQueue 的功能。
 *
 * @param <E> 队列中元素的类型
 */
public interface LbsBlockingQueue<E> {

    /**
     * 将指定元素插入此队列中，如果队列已满，则抛出 IllegalStateException。
     * @param e 要添加的元素
     * @return true (遵从 Collection.add)
     * @throws IllegalStateException 如果队列已满
     * @throws NullPointerException 如果指定元素为 null
     */
    boolean add(E e);

    /**
     * 将指定元素插入此队列的尾部，如果队列已满，则返回 false。
     * @param e 要添加的元素
     * @return 如果成功插入元素，则返回 true；否则返回 false
     * @throws NullPointerException 如果指定元素为 null
     */
    boolean offer(E e);

    /**
     * 将指定元素插入此队列的尾部，如果队列已满，则会阻塞等待，直到有空间可用。
     * @param e 要添加的元素
     * @throws InterruptedException 如果在等待时线程被中断
     * @throws NullPointerException 如果指定元素为 null
     */
    void put(E e) throws InterruptedException;

    /**
     * 将指定元素插入此队列的尾部，如果队列已满，则在指定的等待时间内等待空间可用。
     * @param e       要添加的元素
     * @param timeout 等待的最长时间
     * @param unit    timeout 参数的时间单位
     * @return 如果在超时前成功插入元素，则返回 true；如果超时，则返回 false
     * @throws InterruptedException 如果在等待时线程被中断
     * @throws NullPointerException 如果指定元素为 null
     */
    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 检索并删除此队列的头部，如果队列为空，则会阻塞等待，直到有元素可用。
     * @return 队列的头部元素
     * @throws InterruptedException 如果在等待时线程被中断
     */
    E take() throws InterruptedException;

    /**
     * 检索并删除此队列的头部，如果队列为空，则在指定的等待时间内等待元素可用。
     * @param timeout 等待的最长时间
     * @param unit    timeout 参数的时间单位
     * @return 队列的头部元素；如果在指定时间内队列仍为空，则返回 null
     * @throws InterruptedException 如果在等待时线程被中断
     */
    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 检索并删除此队列的头部，如果队列为空，则返回 null。
     * @return 队列的头部元素，如果队列为空，则返回 null
     */
    E poll();

    /**
     * 检索并删除此队列的头部，如果队列为空，则抛出 NoSuchElementException。
     * @return 队列的头部元素
     * @throws java.util.NoSuchElementException 如果队列为空
     */
    E remove();

    /**
     * 检索但不删除此队列的头部，如果队列为空，则返回 null。
     * @return 队列的头部元素，如果队列为空，则返回 null
     */
    E peek();

    /**
     * 检索但不删除此队列的头部，如果队列为空，则抛出 NoSuchElementException。
     * @return 队列的头部元素
     * @throws java.util.NoSuchElementException 如果队列为空
     */
    E element();

    /**
     * 从此队列中移除所有可用的元素，并将它们添加到给定的集合中。
     * @param c 要传输元素的集合
     * @return 传输的元素数量
     * @throws NullPointerException 如果集合 c 为 null
     */
    int drainTo(Collection<? super E> c);
}