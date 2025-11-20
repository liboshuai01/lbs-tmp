package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class PaymentServiceSafe {

    // 【架构师思维】
    // 预先定义一组锁（比如 128 个），形成一个“锁段”数组
    // 这是一个典型的 "空间换时间" + "锁粒度控制" 的平衡
    private final Object[] locks;
    private final int lockCount;

    public PaymentServiceSafe() {
        this.lockCount = 128; // 根据并发度调整，必须是 2 的幂次方以便位运算优化（这里简单用取模）
        this.locks = new Object[lockCount];
        for (int i = 0; i < lockCount; i++) {
            this.locks[i] = new Object();
        }
    }

    public void pay(String orderId) {
        // 1. 计算哈希槽位：确保同一个 orderId 总是落在同一个槽位
        int hash = Math.abs(orderId.hashCode());
        int index = hash % lockCount;

        // 2. 获取对应的分段锁

        synchronized (locks[index]) {
            // ... 业务逻辑 ...
            System.out.println(Thread.currentThread().getName() + " 正在处理: " + orderId);
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) {}
        }
    }
}
