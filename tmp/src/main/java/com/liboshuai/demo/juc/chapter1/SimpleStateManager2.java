package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SimpleStateManager2 {

    private static final Logger log = LoggerFactory.getLogger(SimpleStateManager2.class);

    private final Map<String, Integer> stateMap = new HashMap<>();

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        int numThreads = 10;
        int numTasks = 1000;
        String key = "flinkJobId";
        int numFinals = numTasks * numThreads;
        SimpleStateManager2 simpleStateManager1 = new SimpleStateManager2();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (int i = 0; i < numFinals; i++) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> simpleStateManager1.updateState(key), executor);
            completableFutures.add(completableFuture);
        }
        CompletableFuture<?>[] completableFutureArray = completableFutures.toArray(new CompletableFuture[0]);
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFutureArray);
        try {
            completableFuture.get(10, TimeUnit.SECONDS);
            log.info("所有任务在10秒在已全部完成!");
        } catch (InterruptedException e) {
            log.warn("主线程在等待时被中断!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("至少一个任务执行失败了! ", e);
        } catch (TimeoutException e) {
            log.warn("所有任务在10秒内未全部完成!");
        }
        ExecutorUtil.close(executor, 10, TimeUnit.SECONDS);
        System.out.println("========================================");
        System.out.println("所有线程执行完毕。");
        System.out.println("预期结果: 10000");
        System.out.println("实际结果: " + simpleStateManager1.getState(key));
    }

    public synchronized void updateState(String key) {
        try {
            Integer value = stateMap.get(key);
            if (value == null) {
                stateMap.put(key, 1);
            } else {
                stateMap.put(key, ++value);
            }
        } finally {
            log.info(" 更新了 " + key + ", new value=" + stateMap.get(key));
        }
    }

    public synchronized Integer getState(String key) {
        return stateMap.get(key);
    }

    public synchronized int getStateSize() {
        return stateMap.size();
    }
}
