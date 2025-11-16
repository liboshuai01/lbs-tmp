package com.liboshuai.demo.juc.chapter3;

import com.liboshuai.demo.function.FunctionUtils;
import com.liboshuai.demo.juc.chapter1.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockSimulator {

    private static final Logger log = LoggerFactory.getLogger(ReadWriteLockSimulator.class);

    public static void main(String[] args) {
        String key = "UUID";
        int taskNums = 10;
        ExecutorService executor = Executors.newFixedThreadPool(taskNums);
//        ConfigRegister configRegister = new ConfigRegister(false);
        ConfigRegister configRegister = new ConfigRegister(true);
        List<CompletableFuture<Void>> readCfList = new ArrayList<>();
        AtomicInteger readCounter = new AtomicInteger(0);
        Instant start = Instant.now();
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Void> cf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(() -> {
                while (readCounter.incrementAndGet() < 100) {
                    String value = configRegister.getConfig(key);
                    log.info("[task]: 读取配置 key-{}, value-{}", key, value);
                }
            }), executor);
            readCfList.add(cf);
        }
        AtomicInteger writeCounter = new AtomicInteger(0);
        CompletableFuture<Void> writeCf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(() -> {
            while (writeCounter.incrementAndGet() < 50) {
                String value = UUID.randomUUID().toString();
                configRegister.updateConfig(key, value);
                log.info("[admin]: 更新配置 key-{}, value-{}", key, value);
            }
        }), executor);
        readCfList.add(writeCf);
        CompletableFuture<?>[] cfArray = readCfList.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> cf = CompletableFuture.allOf(cfArray);
        try {
            cf.get(1, TimeUnit.MINUTES);
            Instant end = Instant.now();
            long millis = Duration.between(start, end).toMillis();
            log.info("所有任务均执行完毕! 耗时: {} 毫秒", millis);
        } catch (InterruptedException e) {
            log.warn("主线程等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少有一个任务执行失败!");
        } catch (TimeoutException e) {
            log.info("超时! 在规定时间1分钟内存在任务未执行完毕");
        }
        ExecutorUtils.close(executor, 1, TimeUnit.MINUTES);
    }

    private static class ConfigRegister {

        private final Map<String, String> configMap = new HashMap<>();
        private final boolean USE_RW_LOCK;
        private final ReentrantLock exclusiveLock = new ReentrantLock();
        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final Lock readLock = readWriteLock.readLock();
        private final Lock writeLock = readWriteLock.writeLock();


        private ConfigRegister(boolean useRwLock) {
            USE_RW_LOCK = useRwLock;
        }

        public String getConfig(String key) throws InterruptedException {
            Lock lock = USE_RW_LOCK ? readLock : exclusiveLock;
            lock.lock();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                return configMap.getOrDefault(key, "Default Value");
            } finally {
                lock.unlock();
            }
        }

        public void updateConfig(String key, String value) throws InterruptedException {
            Lock lock = USE_RW_LOCK ? writeLock : exclusiveLock;
            lock.lock();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                configMap.put(key, value);
            } finally {
                lock.unlock();
            }
        }

    }

}
