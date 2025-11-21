package com.juc.rpc.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 接口定义
public interface StockService {
    boolean deductStock(String commodityCode, int count);
    int getStock(String commodityCode);
}

