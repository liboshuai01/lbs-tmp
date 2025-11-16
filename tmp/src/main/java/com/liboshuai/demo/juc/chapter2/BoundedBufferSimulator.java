package com.liboshuai.demo.juc.chapter2;

import com.liboshuai.demo.juc.chapter1.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBufferSimulator {
    private static final Logger log = LoggerFactory.getLogger(BoundedBufferSimulator.class);

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        int threadNums = 10;
        FlinkInputChannel<String> stringFlinkInputChannel = new FlinkInputChannel<>(5);
        ExecutorService executor = Executors.newFixedThreadPool(threadNums);
        List<CompletableFuture<Void>> producerCfList = new ArrayList<>();
        AtomicInteger producerCounter = new AtomicInteger();
        int taskNums = 30;
        for (int i = 0; i < 3; i++) {
            CompletableFuture<Void> producerCf = CompletableFuture.runAsync(() -> {
                while (true) {
                    try {
                        int count = producerCounter.incrementAndGet();
                        if (count > taskNums) {
                            break;
                        }
                        stringFlinkInputChannel.put(count + "");
                        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(1000) + 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, executor);
            producerCfList.add(producerCf);
        }
        List<CompletableFuture<Void>> consumerCfList = new ArrayList<>();
        AtomicInteger consumerCounter = new AtomicInteger();
        for (int i = 0; i < 6; i++) {
            CompletableFuture<Void> consumerCf = CompletableFuture.runAsync(() -> {
                while (true) {
                    try {
                        int count = consumerCounter.incrementAndGet();
                        if (count > taskNums) {
                            break;
                        }
                        String data = stringFlinkInputChannel.get();
                        if (data == null) {
                            break;
                        }
                        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(1000) + 2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, executor);
            consumerCfList.add(consumerCf);
        }

        List<CompletableFuture<?>> allCfList = new ArrayList<>();
        allCfList.addAll(producerCfList);
        allCfList.addAll(consumerCfList);
        CompletableFuture<?>[] allCfArray = allCfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allCf = CompletableFuture.allOf(allCfArray);
        try {
            allCf.get(1, TimeUnit.MINUTES);
            log.info("所有任务均在规定时间内执行完毕!");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.warn("至少有一个任务执行失败!", e);
        } catch (TimeoutException e) {
            log.warn("超时! 所有任务未在规定时间内执行完毕!");
        }
        ExecutorUtil.close(executor, 1, TimeUnit.MINUTES);
    }

    private static class FlinkInputChannel<T> {
        private final Queue<T> buffer = new LinkedList<>();
        private final int capacity;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        FlinkInputChannel(int capacity) {
            this.capacity = capacity;
        }

        public void put(T data) throws InterruptedException {
            lock.lock();
            try {
                while (buffer.size() == capacity) {
                    log.info("[生产者]: 缓冲区已满! 等待消费者取走数据...");
                    if (!notFull.await(5, TimeUnit.SECONDS)) {
                        // 超过指定时间没有数据被消费, 表示消费者已经被提前关闭了
                        log.info("[生产者]: 超过指定时间没有数据被消费, 关闭生产者");
                        return;
                    }
                }
                buffer.add(data);
                log.info("[生产者]: 放入数据 {}, 当前数据总量 {}", data, buffer.size());
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public T get() throws InterruptedException {
            lock.lock();
            try {
                while (buffer.isEmpty()) {
                    log.info("[消费者]: 缓冲区已空! 等待生产者产生数据...");
                    if (!notEmpty.await(5, TimeUnit.SECONDS)) {
                        // 超过指定时间没有数据产生, 表示生产者已经被提前关闭了
                        log.info("[消费者]: 超过指定时间没有数据产生, 关闭消费者");
                        return null;
                    }
                }
                T data = buffer.remove();
                log.info("[消费者]: 取出数据 {}, 当前数据总量 {}", data, buffer.size());
                notFull.signal();
                return data;
            } finally {
                lock.unlock();
            }
        }
    }
}
