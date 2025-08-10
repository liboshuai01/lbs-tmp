package com.liboshuai.demo.safe.collection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class MapDemo {
    public static void main(String[] args) {
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(30);
        MyMap myMap = new MyMap();

        CompletableFuture<?>[] futures = new CompletableFuture[30];
        for (int i = 0; i < 30; i++) {
            futures[i] = CompletableFuture.runAsync(new UpdateMapTask(myMap), newFixedThreadPool);
        }

        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            System.out.println("\n在并发操作中发生了异常: " + e);
        } finally {
            // 关闭线程池
            newFixedThreadPool.shutdown();
            System.out.println("\n--- 所有线程执行完毕，关闭线程池 ---");
        }
    }
}

class MyMap {
    private final Map<String, String> map = new ConcurrentHashMap<>();
    
    public void update(String key, String value) {
        map.put(key, value);
        System.out.printf("[%s] 线程添加了一个元素，现在 set 集合存在的元素都有: [%s]%n", Thread.currentThread().getName(), map);
    }
}

class UpdateMapTask implements Runnable {

    private final MyMap myMap;

    public UpdateMapTask(MyMap myMap) {
        this.myMap = myMap;
    }

    @Override
    public void run() {
        myMap.update(UUID.randomUUID().toString().substring(0,4), UUID.randomUUID().toString().substring(0,4));
    }

}