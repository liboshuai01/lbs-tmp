package com.liboshuai.demo.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LbsLinkedBlockingQueue 的单元测试类。
 */
@DisplayName("LbsLinkedBlockingQueue 功能测试")
class LbsLinkedBlockingQueueTest {

    private LbsLinkedBlockingQueue<Integer> queue;
    private final int CAPACITY = 5;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，都创建一个容量为 5 的新队列
        queue = new LbsLinkedBlockingQueue<>(CAPACITY);
    }

    @Test
    @DisplayName("测试构造函数 - 非法容量")
    void testConstructorWithInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LbsLinkedBlockingQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LbsLinkedBlockingQueue<>(-1));
    }

    @Test
    @DisplayName("测试 add - 队列未满")
    void testAdd_whenQueueNotFull() {
        assertTrue(queue.add(1));
        assertEquals(1, queue.peek());
    }

    @Test
    @DisplayName("测试 add - 队列已满")
    void testAdd_whenQueueFull() {
        for (int i = 0; i < CAPACITY; i++) {
            queue.add(i);
        }
        assertThrows(IllegalStateException.class, () -> queue.add(99));
    }

    @Test
    @DisplayName("测试 add - 添加 null 元素")
    void testAdd_withNullElement() {
        assertThrows(NullPointerException.class, () -> queue.add(null));
    }

    @Test
    @DisplayName("测试 offer - 队列未满")
    void testOffer_whenQueueNotFull() {
        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2));
    }

    @Test
    @DisplayName("测试 offer - 队列已满")
    void testOffer_whenQueueFull() {
        for (int i = 0; i < CAPACITY; i++) {
            queue.offer(i);
        }
        assertFalse(queue.offer(99));
    }

    @Test
    @DisplayName("测试 remove - 队列非空")
    void testRemove_whenQueueNotEmpty() {
        queue.add(1);
        queue.add(2);
        assertEquals(1, queue.remove());
        assertEquals(2, queue.remove());
    }

    @Test
    @DisplayName("测试 remove - 队列为空")
    void testRemove_whenQueueEmpty() {
        assertThrows(NoSuchElementException.class, () -> queue.remove());
    }

    @Test
    @DisplayName("测试 poll - 队列为空")
    void testPoll_whenQueueEmpty() {
        assertNull(queue.poll());
    }

    @Test
    @DisplayName("测试 element - 队列非空")
    void testElement_whenQueueNotEmpty() {
        queue.add(1);
        assertEquals(1, queue.element());
        // 确认元素没有被移除
        assertEquals(1, queue.peek());
    }

    @Test
    @DisplayName("测试 element - 队列为空")
    void testElement_whenQueueEmpty() {
        assertThrows(NoSuchElementException.class, () -> queue.element());
    }

    @Test
    @DisplayName("测试 peek - 队列为空")
    void testPeek_whenQueueEmpty() {
        assertNull(queue.peek());
    }

    @Test
    @DisplayName("测试 put - 当队列满时阻塞，并在 take 后解除阻塞")
    void testPut_blocksWhenFullAndUnblocksAfterTake() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }

        CountDownLatch latch = new CountDownLatch(1);
        Thread producerThread = new Thread(() -> {
            try {
                // 此时队列已满，put 操作会阻塞
                queue.put(99);
                latch.countDown(); // 如果成功 put，则 countDown
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producerThread.start();

        // 等待一小段时间，确保 producerThread 已经开始并阻塞在 put()
        Thread.sleep(100);
        assertEquals(Thread.State.WAITING, producerThread.getState());

        // 消费者取走一个元素，为生产者腾出空间
        assertEquals(0, queue.take());

        // 生产者线程应该被唤醒并成功插入元素
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, queue.peek()); // 确认 take 之后队列头变了，但 put 的元素在队尾
    }


    @Test
    @DisplayName("测试 take - 当队列为空时阻塞，并在 put 后解除阻塞")
    void testTake_blocksWhenEmptyAndUnblocksAfterPut() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Integer[] takenElement = new Integer[1];

        Thread consumerThread = new Thread(() -> {
            try {
                // 此时队列为空，take 操作会阻塞
                takenElement[0] = queue.take();
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumerThread.start();

        // 等待一小段时间，确保 consumerThread 已经开始并阻塞在 take()
        Thread.sleep(100);
        assertEquals(Thread.State.WAITING, consumerThread.getState());

        // 生产者放入一个元素
        queue.put(88);

        // 消费者线程应该被唤醒并成功取到元素
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(88, takenElement[0]);
    }

    @Test
    @DisplayName("测试 offer 带超时 - 队列满时超时返回 false")
    void testOfferWithTimeout_returnsFalseAfterTimeout() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        assertFalse(queue.offer(99, 100, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("测试 poll 带超时 - 队列空时超时返回 null")
    void testPollWithTimeout_returnsNullAfterTimeout() throws InterruptedException {
        assertNull(queue.poll(100, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("测试阻塞方法的中断 - put")
    void testInterrupt_duringPut() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }

        Thread t = new Thread(() -> {
            try {
                queue.put(100);
                fail("put 操作在中断后没有抛出 InterruptedException");
            } catch (InterruptedException e) {
                // 这是预期的行为
                assertTrue(true);
            }
        });

        t.start();
        Thread.sleep(100); // 确保线程已阻塞
        t.interrupt();
        t.join();
    }

    @Test
    @DisplayName("测试阻塞方法的中断 - take")
    void testInterrupt_duringTake() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                queue.take();
                fail("take 操作在中断后没有抛出 InterruptedException");
            } catch (InterruptedException e) {
                // 这是预期的行为
                assertTrue(true);
            }
        });

        t.start();
        Thread.sleep(100); // 确保线程已阻塞
        t.interrupt();
        t.join();
    }

    @Test
    @DisplayName("测试 drainTo - 将队列元素排空到集合")
    void testDrainTo() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        List<Integer> list = new ArrayList<>();
        int drainedCount = queue.drainTo(list);

        assertEquals(CAPACITY, drainedCount);
        assertEquals(CAPACITY, list.size());
        assertNull(queue.poll()); // 确认原队列已空
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    @DisplayName("测试 drainTo - 队列为空")
    void testDrainTo_whenQueueEmpty() {
        List<Integer> list = new ArrayList<>();
        int drainedCount = queue.drainTo(list);
        assertEquals(0, drainedCount);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("多生产者多消费者并发测试")
    @Timeout(10) // 设置一个测试超时时间，防止因死锁等问题导致测试挂起
    void testMultiProducerMultiConsumer() throws InterruptedException {
        final LbsLinkedBlockingQueue<Integer> concurrentQueue = new LbsLinkedBlockingQueue<>(100);
        final int producerCount = 4;
        final int consumerCount = 4;
        final int itemsPerProducer = 1000;
        final int totalItems = producerCount * itemsPerProducer;
        // 使用一个线程安全集合来收集所有消费到的元素
        final ConcurrentLinkedQueue<Integer> consumedItems = new ConcurrentLinkedQueue<>();
        final ExecutorService executor = Executors.newFixedThreadPool(producerCount + consumerCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(producerCount + consumerCount);

        // 创建生产者任务
        for (int i = 0; i < producerCount; i++) {
            int producerId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    for (int j = 0; j < itemsPerProducer; j++) {
                        // 每个生产者生产唯一的数字
                        int item = producerId * itemsPerProducer + j;
                        concurrentQueue.put(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 创建消费者任务
        for (int i = 0; i < consumerCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    // 持续消费直到生产者生产完毕且队列为空
                    while (doneLatch.getCount() > consumerCount || concurrentQueue.peek() != null) {
                        Integer item = concurrentQueue.poll(10, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            consumedItems.add(item);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 所有线程同时开始
        boolean ignore = doneLatch.await(5, TimeUnit.SECONDS); // 等待所有任务完成
        executor.shutdownNow();

        // 验证结果
        assertEquals(totalItems, consumedItems.size(), "消费的元素总数不匹配");

        List<Integer> expectedItems = IntStream.range(0, totalItems).boxed().collect(Collectors.toList());
        List<Integer> actualItems = new ArrayList<>(consumedItems);

        // 对比两个列表的内容，忽略顺序
        expectedItems.sort(null);
        actualItems.sort(null);

        assertEquals(expectedItems, actualItems, "消费的元素集合与生产的元素集合不一致");
    }
}