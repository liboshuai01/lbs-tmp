package com.liboshuai.demo.juc.problem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WalletService {

    // 模拟数据库/缓存，Key是用户ID，Value是余额
    // 注意：这是一个共享的可变状态
    private final Map<String, Double> balanceMap = new ConcurrentHashMap<>();

    /**
     * 初始化测试数据
     */
    public WalletService() {
        balanceMap.put("User_A", 100.00); // 用户A有100元
    }

    /**
     * 扣款业务方法
     * @param userId 用户ID
     * @param amount 扣款金额
     */
    public void deduct(String userId, double amount) {

        balanceMap.compute(userId, (id, balance) -> {
            // [步骤2]：前置校验
            if (balance == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            // [步骤3]：余额检查
            if (balance < amount) {
                throw new RuntimeException("余额不足！当前余额: " + balance);
            }

            // [步骤4]：模拟业务处理耗时（关键点：让CPU切换，扩大并发问题暴露概率）
            try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) {}

            // [步骤5]：执行扣减并更新状态
            double newBalance = balance - amount;
            System.out.println("线程[" + Thread.currentThread().getName() + "] 扣款成功, 剩余: " + newBalance);
            return newBalance;
        });

    }
}