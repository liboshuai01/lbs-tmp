package com.juc.rpc.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 实现类
@Slf4j
public class StockServiceImpl implements StockService {

    private int stock = 1000; // 初始库存

    // JUC: ReentrantLock (可重入锁)
    // 比 synchronized 更灵活，支持 tryLock (非阻塞尝试) 和 lockInterruptibly (可中断)
    private final Lock lock = new ReentrantLock();

    // JUC: Condition
    // 配合 Lock 使用，模拟“库存不足等待补货”的场景（此处仅展示用法，实际扣减不一定需要wait）
    private final Condition stockEnough = lock.newCondition();

    @Override
    public boolean deductStock(String commodityCode, int count) {
        // JUC: 使用 tryLock 防止死锁，如果 3秒拿不到锁就放弃
        try {
            if (lock.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    if (stock >= count) {
                        // 模拟业务耗时
                        Thread.sleep(5);
                        stock -= count;
                        log.info("Stock deducted. Remaining: {}", stock);
                        return true;
                    } else {
                        log.warn("Stock not enough!");
                        return false;
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("System busy, could not acquire lock");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public int getStock(String commodityCode) {
        return stock;
    }
}
