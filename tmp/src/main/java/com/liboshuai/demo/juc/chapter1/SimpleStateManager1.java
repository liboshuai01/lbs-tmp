package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleStateManager1 {

    private static final Logger log = LoggerFactory.getLogger(SimpleStateManager1.class);

    private final Map<String, Integer> stateMap = new HashMap<>();

    private final CountDownLatch countDownLatch;

    public SimpleStateManager1(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
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
            countDownLatch.countDown();
            log.info(" 更新了 " + key + ", new value=" + stateMap.get(key));
        }
    }

    public synchronized Integer getState(String key) {
        return stateMap.get(key);
    }

    public synchronized int getStateSize() {
        return stateMap.size();
    }

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10;
        int numTasks = 1000;
        String key = "flinkJobId";
        int numFinals = numTasks * numThreads;
        CountDownLatch countDownLatch = new CountDownLatch(numFinals);
        SimpleStateManager1 simpleStateManager1 = new SimpleStateManager1(countDownLatch);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numFinals; i++) {
            executor.submit(() -> simpleStateManager1.updateState(key));
        }
        if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
            log.warn("所有任务在10秒内未全部完成!");
        } else {
            log.info("所有任务在10秒在已全部完成!");
        }
        ExecutorUtils.close(executor, 10, TimeUnit.SECONDS);
        System.out.println("========================================");
        System.out.println("所有线程执行完毕。");
        System.out.println("预期结果: 10000");
        System.out.println("实际结果: " + simpleStateManager1.getState(key));
    }
}
