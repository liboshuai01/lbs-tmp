package com.liboshuai.demo.juc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleSlotPool {

    private final Queue<String> availableSlots;
    private final int maxSlots;

    // TODO: 定义锁和条件变量
    private final ReentrantLock lock = new ReentrantLock();
    // 思考：这里需要几个 Condition？

    private final Condition notEmpty;

    public SimpleSlotPool(int maxSlots) {
        this.maxSlots = maxSlots;
        this.availableSlots = new LinkedList<>();
        for (int i = 0; i < maxSlots; i++) {
            availableSlots.add("Slot-" + i);
        }
        // TODO: 初始化 Condition
        notEmpty = lock.newCondition();
    }

    // 申请资源：如果没资源，需等待
    public String acquireSlot() throws InterruptedException {
        lock.lock();
        try {
            // TODO: 1. 判断是否有资源
            // TODO: 2. 如果没有，如何等待？
            // TODO: 3. 获取资源并返回
            while (availableSlots.isEmpty()) {
                notEmpty.await();
            }
            return availableSlots.poll();
//            return null; // 占位
        } finally {
            lock.unlock();
        }
    }

    // 归还资源
    public void releaseSlot(String slot) {
        lock.lock();
        try {
            // TODO: 1. 归还资源
            // TODO: 2. 既然有资源了，下一步该通知谁？
            while (availableSlots.size() >= maxSlots) {
                throw new IllegalStateException("池已满！这意味着引用计数中存在一个 bug。");
            }
            if (availableSlots.add(slot)) {
                notEmpty.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
