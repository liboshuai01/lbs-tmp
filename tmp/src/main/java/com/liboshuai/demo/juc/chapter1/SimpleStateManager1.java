package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleStateManager1 {

    private static final Logger log = LoggerFactory.getLogger(SimpleStateManager1.class);

    private final Map<String, Integer> stateMap = new HashMap<>();

    public synchronized void updateState(String key) {
        Integer value = stateMap.get(key);
        if (value == null) {
            stateMap.put(key, 0);
        } else {
            stateMap.put(key, ++value);
        }
        log.info(" 更新了 " + key + ", new value=" + stateMap.get(key));
    }

    public synchronized Integer getState(String key) {
        return stateMap.get(key);
    }

    public synchronized int getStateSize() {
        return stateMap.size();
    }

    public static void main(String[] args) {
        SimpleStateManager1 simpleStateManager1 = new SimpleStateManager1();

        ExecutorService executor = Executors.newFixedThreadPool(10);

    }
}
