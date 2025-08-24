package com.liboshuai.demo.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LbsSynchronousQueue 的单元测试类。
 */
@DisplayName("LbsSynchronousQueue 单元测试")
class LbsSynchronousQueueTest {

    private LbsBlockingQueue<String> queue;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        // 为每个测试创建一个新的队列实例和一个线程池
        queue = new LbsSynchronousQueue<>();
        executor = Executors.newCachedThreadPool();
    }

    @Test
    @DisplayName("测试 put 和 take 的基本交互")
    @Timeout(2)
    void testPutAndTake() throws Exception {
        final String testItem = "test-item";
        final AtomicReference<String> receivedItem = new AtomicReference<>();

        // 提交一个消费者任务，它会调用 take() 并阻塞
        Future<?> consumerFuture = executor.submit(() -> {
            try {
                receivedItem.set(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 等待一小段时间，确保消费者已经阻塞在 take() 上
        Thread.sleep(100);

        // 提交一个生产者任务，它会调用 put()
        Future<?> producerFuture = executor.submit(() -> {
            try {
                queue.put(testItem);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 等待两个任务都完成
        producerFuture.get();
        consumerFuture.get();

        // 验证消费者是否收到了正确的元素
        assertEquals(testItem, receivedItem.get());
    }

    @Test
    @DisplayName("测试 take 和 put 的反向交互")
    @Timeout(2)
    void testTakeAndPut() throws Exception {
        final String testItem = "test-item-reverse";
        final AtomicReference<String> receivedItem = new AtomicReference<>();

        // 提交一个生产者任务，它会调用 put() 并阻塞
        Future<?> producerFuture = executor.submit(() -> {
            try {
                queue.put(testItem);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 等待一小段时间，确保生产者已经阻塞在 put() 上
        Thread.sleep(100);

        // 提交一个消费者任务，它会调用 take()
        Future<?> consumerFuture = executor.submit(() -> {
            try {
                receivedItem.set(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 等待两个任务都完成
        producerFuture.get();
        consumerFuture.get();

        // 验证消费者是否收到了正确的元素
        assertEquals(testItem, receivedItem.get());
    }

    @Test
    @DisplayName("测试 offer 带超时 - 成功场景")
    @Timeout(2)
    void testOfferWithTimeout_Success() throws Exception {
        final String testItem = "offer-success";

        // 启动一个消费者，在短暂延迟后取走元素
        executor.submit(() -> {
            try {
                Thread.sleep(100);
                queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 调用 offer，预期会成功
        boolean result = queue.offer(testItem, 500, TimeUnit.MILLISECONDS);
        assertTrue(result, "offer 应该在消费者准备好时成功");
    }

    @Test
    @DisplayName("测试 offer 带超时 - 失败场景")
    @Timeout(1)
    void testOfferWithTimeout_Failure() throws InterruptedException {
        // 没有消费者，offer 调用应该在超时后失败
        boolean result = queue.offer("offer-fail", 100, TimeUnit.MILLISECONDS);
        assertFalse(result, "在没有消费者的情况下，offer 应该超时失败");
    }

    @Test
    @DisplayName("测试 poll 带超时 - 成功场景")
    @Timeout(2)
    void testPollWithTimeout_Success() throws Exception {
        final String testItem = "poll-success";

        // 启动一个生产者，在短暂延迟后放入元素
        executor.submit(() -> {
            try {
                Thread.sleep(100);
                queue.put(testItem);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 调用 poll，预期会成功取到元素
        String receivedItem = queue.poll(500, TimeUnit.MILLISECONDS);
        assertEquals(testItem, receivedItem, "poll 应该在生产者放入元素后成功");
    }

    @Test
    @DisplayName("测试 poll 带超时 - 失败场景")
    @Timeout(1)
    void testPollWithTimeout_Failure() throws InterruptedException {
        // 没有生产者，poll 调用应该在超时后返回 null
        String receivedItem = queue.poll(100, TimeUnit.MILLISECONDS);
        assertNull(receivedItem, "在没有生产者的情况下，poll 应该超时并返回 null");
    }

    @Test
    @DisplayName("测试 offer (无超时) 总是返回 false")
    void testOfferWithoutTimeout() {
        assertFalse(queue.offer("item"), "对于 SynchronousQueue，无超时的 offer 应该总是返回 false");
    }

    @Test
    @DisplayName("测试 poll (无超时) 总是返回 null")
    void testPollWithoutTimeout() {
        assertNull(queue.poll(), "对于 SynchronousQueue，无超时的 poll 应该总是返回 null");
    }

    @Test
    @DisplayName("测试 add 方法总是抛出 IllegalStateException")
    void testAdd() {
        assertThrows(IllegalStateException.class, () -> queue.add("item"), "add 方法应该总是抛出 IllegalStateException");
    }

    @Test
    @DisplayName("测试 remove 方法总是抛出 NoSuchElementException")
    void testRemove() {
        assertThrows(NoSuchElementException.class, () -> queue.remove(), "remove 方法应该总是抛出 NoSuchElementException");
    }

    @Test
    @DisplayName("测试 peek 方法总是返回 null")
    void testPeek() {
        assertNull(queue.peek(), "peek 方法应该总是返回 null");
    }

    @Test
    @DisplayName("测试 element 方法总是抛出 NoSuchElementException")
    void testElement() {
        assertThrows(NoSuchElementException.class, () -> queue.element(), "element 方法应该总是抛出 NoSuchElementException");
    }

    @Test
    @DisplayName("测试 drainTo 方法总是返回 0")
    void testDrainTo() {
        List<String> list = new ArrayList<>();
        assertEquals(0, queue.drainTo(list), "drainTo 应该总是返回 0 并且不添加任何元素");
        assertTrue(list.isEmpty(), "目标集合在 drainTo 之后应该仍然为空");
    }

    @Test
    @DisplayName("测试 put 方法在线程被中断时抛出 InterruptedException")
    @Timeout(1)
    void testPutInterruption() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            try {
                queue.put("item");
            } catch (InterruptedException e) {
                // 这是预期的行为
                latch.countDown();
            }
        });

        t.start();
        Thread.sleep(100); // 确保线程 t 已经阻塞在 put() 上
        t.interrupt();

        assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "被中断的 put 操作应该快速抛出 InterruptedException");
    }

    @Test
    @DisplayName("测试 take 方法在线程被中断时抛出 InterruptedException")
    @Timeout(1)
    void testTakeInterruption() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                // 这是预期的行为
                latch.countDown();
            }
        });

        t.start();
        Thread.sleep(100); // 确保线程 t 已经阻塞在 take() 上
        t.interrupt();

        assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "被中断的 take 操作应该快速抛出 InterruptedException");
    }

    @Test
    @DisplayName("测试 put 方法不允许 null 元素")
    void testPutNull() {
        assertThrows(NullPointerException.class, () -> queue.put(null));
    }

    @Test
    @DisplayName("测试 offer 方法不允许 null 元素")
    void testOfferNull() {
        assertThrows(NullPointerException.class, () -> queue.offer(null));
        assertThrows(NullPointerException.class, () -> queue.offer(null, 1, TimeUnit.SECONDS));
    }
}
