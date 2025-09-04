package com.liboshuai.demo.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LbsArrayBlockingQueue 的单元测试类
 */
class LbsArrayBlockingQueueTest {

    private LbsArrayBlockingQueue<Integer> queue;
    private final int CAPACITY = 5;

    @BeforeEach
    void setUp() {
        queue = new LbsArrayBlockingQueue<>(CAPACITY);
    }

    @Test
    void testConstructorWithInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LbsArrayBlockingQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LbsArrayBlockingQueue<>(-1));
    }

    // --- 单线程测试 ---

    @Test
    void testAddAndPoll() {
        assertTrue(queue.add(1));
        assertTrue(queue.add(2));
        assertEquals(1, queue.poll());
        assertEquals(2, queue.poll());
        assertNull(queue.poll());
    }

    @Test
    void testAddThrowsExceptionWhenFull() {
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(queue.add(i));
        }
        assertThrows(IllegalStateException.class, () -> queue.add(99));
    }

    @Test
    void testAddThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> queue.add(null));
    }

    @Test
    void testOffer() {
        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2));
        assertEquals(1, queue.poll());
        assertEquals(2, queue.poll());
    }

    @Test
    void testOfferReturnsFalseWhenFull() {
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(queue.offer(i));
        }
        assertFalse(queue.offer(99));
    }

    @Test
    void testOfferThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> queue.offer(null));
    }


    @Test
    void testRemove() {
        queue.add(1);
        assertEquals(1, queue.remove());
    }

    @Test
    void testRemoveThrowsExceptionWhenEmpty() {
        assertThrows(NoSuchElementException.class, () -> queue.remove());
    }

    @Test
    void testPeekAndElement() {
        assertNull(queue.peek());
        assertThrows(NoSuchElementException.class, () -> queue.element());

        queue.add(1);
        queue.add(2);
        assertEquals(1, queue.peek());
        assertEquals(1, queue.element());
        // 验证元素没有被移除
        assertEquals(1, queue.poll());
        assertEquals(2, queue.peek());
    }

    @Test
    void testCircularBehavior() throws InterruptedException {
        // 填满队列
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        // 取出两个元素
        assertEquals(0, queue.take());
        assertEquals(1, queue.take());

        // 再放入两个元素，这时 putIndex 应该回绕了
        queue.put(CAPACITY);
        queue.put(CAPACITY + 1);

        // 验证队列内容顺序
        assertEquals(2, queue.take());
        assertEquals(3, queue.take());
        assertEquals(4, queue.take());
        assertEquals(CAPACITY, queue.take());
        assertEquals(CAPACITY + 1, queue.take());
        assertNull(queue.poll());
    }

    @Test
    void testDrainTo() {
        for (int i = 0; i < CAPACITY; i++) {
            queue.add(i);
        }
        List<Integer> list = new ArrayList<>();
        int drainedCount = queue.drainTo(list);

        assertEquals(CAPACITY, drainedCount);
        assertEquals(CAPACITY, list.size());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(i, list.get(i));
        }
        assertNull(queue.poll()); // 队列应该空了
    }

    @Test
    void testDrainToEmptyQueue() {
        List<Integer> list = new ArrayList<>();
        int drainedCount = queue.drainTo(list);
        assertEquals(0, drainedCount);
        assertTrue(list.isEmpty());
    }

    @Test
    void testDrainToWithNullCollection() {
        assertThrows(NullPointerException.class, () -> queue.drainTo(null));
    }

    // --- 多线程/阻塞测试 ---

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testTakeBlocksUntilElementAvailable() throws InterruptedException, ExecutionException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);

        Future<Integer> future = executor.submit(() -> {
            latch.countDown();
            return queue.take(); // 会在这里阻塞
        });

        latch.await(); // 确保 take() 已经开始执行并阻塞
        Thread.sleep(100); // 稍微等待，增加测试的确定性
        assertFalse(future.isDone());

        queue.put(99);

        assertEquals(99, future.get());
        executor.shutdown();
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testPutBlocksUntilSpaceAvailable() throws InterruptedException, ExecutionException {
        // 填满队列
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);

        Future<?> future = executor.submit(() -> {
            latch.countDown();
            try {
                queue.put(99); // 会在这里阻塞
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        latch.await();
        Thread.sleep(100);
        assertFalse(future.isDone());

        assertEquals(0, queue.take()); // 腾出空间

        future.get(); // put 操作现在应该可以完成了
        assertEquals(1, queue.take()); // 继续消费，验证队列状态
        executor.shutdown();
    }

    @Test
    void testTakeIsInterruptible() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                latch.countDown();
                queue.take();
                fail("take() should have thrown InterruptedException");
            } catch (InterruptedException e) {
                // 这是期望的行为
            }
        });

        t.start();
        latch.await();
        Thread.sleep(100); // 确保线程已经阻塞在 take()
        t.interrupt();
        t.join(1000); // 等待线程结束
        assertFalse(t.isAlive());
    }

    @Test
    void testPutIsInterruptible() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                latch.countDown();
                queue.put(99);
                fail("put() should have thrown InterruptedException");
            } catch (InterruptedException e) {
                // 这是期望的行为
            }
        });

        t.start();
        latch.await();
        Thread.sleep(100); // 确保线程已经阻塞在 put()
        t.interrupt();
        t.join(1000);
        assertFalse(t.isAlive());
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testPollWithTimeoutReturnsNull() throws InterruptedException {
        // 在空队列上调用，应该超时并返回 null
        assertNull(queue.poll(100, TimeUnit.MILLISECONDS));
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testPollWithTimeoutReturnsElement() throws InterruptedException, ExecutionException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);

        Future<Integer> future = executor.submit(() -> {
            latch.countDown();
            return queue.poll(500, TimeUnit.MILLISECONDS);
        });

        latch.await();
        Thread.sleep(100); // 等待，确保 poll 正在阻塞
        queue.put(88);

        assertEquals(88, future.get());
        executor.shutdown();
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testOfferWithTimeoutReturnsFalse() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        // 在满队列上调用，应该超时并返回 false
        assertFalse(queue.offer(99, 100, TimeUnit.MILLISECONDS));
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testOfferWithTimeoutReturnsTrue() throws InterruptedException, ExecutionException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);

        Future<Boolean> future = executor.submit(() -> {
            latch.countDown();
            return queue.offer(88, 500, TimeUnit.MILLISECONDS);
        });

        latch.await();
        Thread.sleep(100);
        queue.take(); // 腾出空间

        assertTrue(future.get());
        executor.shutdown();
    }
}