package com.liboshuai.demo.threadpool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LbsThreadPoolExecutor V1 版本的单元测试
 */
class LbsThreadPoolExecutorTest {

    @Test
    @DisplayName("构造函数: 当 poolSize 为 0 时, 应抛出 IllegalArgumentException")
    void constructor_whenPoolSizeIsZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LbsThreadPoolExecutor(0, new LinkedBlockingQueue<>());
        }, "Pool size must be greater than 0");
    }

    @Test
    @DisplayName("构造函数: 当 poolSize 为负数时, 应抛出 IllegalArgumentException")
    void constructor_whenPoolSizeIsNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LbsThreadPoolExecutor(-1, new LinkedBlockingQueue<>());
        }, "Pool size must be greater than 0");
    }

    @Test
    @DisplayName("构造函数: 当 workQueue 为 null 时, 应抛出 NullPointerException")
    void constructor_whenWorkQueueIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new LbsThreadPoolExecutor(5, null);
        }, "Work queue cannot be null");
    }

    @Test
    @DisplayName("execute: 当提交的任务为 null 时, 应抛出 NullPointerException")
    void execute_whenTaskIsNull_throwsNullPointerException() {
        // given
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(1, new LinkedBlockingQueue<>());

        // when & then
        assertThrows(NullPointerException.class, () -> {
            executor.execute(null);
        });
    }

    @Test
    @DisplayName("execute: 提交单个任务, 任务应被成功执行")
    @Timeout(value = 2, unit = TimeUnit.SECONDS) // 设置超时防止测试因线程阻塞而卡死
    void execute_singleTask_shouldBeExecuted() throws InterruptedException {
        // given
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(1, new LinkedBlockingQueue<>());
        CountDownLatch latch = new CountDownLatch(1); // 用于等待任务执行完成
        AtomicInteger counter = new AtomicInteger(0);

        // when
        executor.execute(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        // then
        // 等待任务执行完成
        assertTrue(latch.await(1, TimeUnit.SECONDS), "任务在规定时间内未执行完成");
        assertEquals(1, counter.get(), "任务计数器值不为1, 说明任务未被正确执行");
    }

    @Test
    @DisplayName("execute: 提交多个任务, 所有任务都应被执行")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void execute_multipleTasks_allTasksShouldBeExecuted() throws InterruptedException {
        // given
        final int poolSize = 3;
        final int taskCount = 10;
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(poolSize, new LinkedBlockingQueue<>());
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        // when
        for (int i = 0; i < taskCount; i++) {
            executor.execute(() -> {
                try {
                    // 模拟耗时操作
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        // then
        assertTrue(latch.await(3, TimeUnit.SECONDS), "并非所有任务都在规定时间内执行完成");
        assertEquals(taskCount, counter.get(), "已执行的任务数量与提交的任务数量不匹配");
    }


    @Test
    @DisplayName("线程复用: 提交大量任务, 实际工作的线程数应等于池大小")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void threadReuse_executingManyTasks_workerThreadCountShouldEqualPoolSize() throws InterruptedException {
        // given
        final int poolSize = 4;
        final int taskCount = 100;
        LbsThreadPoolExecutor executor = new LbsThreadPoolExecutor(poolSize, new LinkedBlockingQueue<>());
        CountDownLatch latch = new CountDownLatch(taskCount);
        // 使用 ConcurrentHashMap 来记录执行过任务的线程名称
        ConcurrentHashMap<String, Boolean> threadNames = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < taskCount; i++) {
            executor.execute(() -> {
                threadNames.put(Thread.currentThread().getName(), true);
                try {
                    // 模拟少量工作
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        // then
        assertTrue(latch.await(4, TimeUnit.SECONDS), "并非所有任务都在规定时间内执行完成");
        // 实际执行任务的线程数量应该等于线程池的大小
        assertEquals(poolSize, threadNames.size(), "执行任务的线程数不等于池大小, 说明线程复用可能存在问题");
    }
}