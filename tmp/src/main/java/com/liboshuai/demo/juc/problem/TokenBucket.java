package com.liboshuai.demo.juc.problem;

public class TokenBucket {

    private int tokens;
    private final int maxTokens;

    public TokenBucket(int maxTokens) {
        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
    }

    /**
     * 申请令牌 (消费者)
     */
    public synchronized void acquire() throws InterruptedException {
        // [问题点 A]：这里使用了 if 来判断条件
        if (tokens <= 0) {
            System.out.println(Thread.currentThread().getName() + " 发现没令牌了，进入等待...");
            this.wait();
        }

        // [问题点 B]：被唤醒后直接扣减
        tokens--;
        System.out.println(Thread.currentThread().getName() + " 拿到令牌，剩余: " + tokens);
    }

    /**
     * 归还令牌 (生产者)
     */
    public synchronized void release() {
        if (tokens < maxTokens) {
            tokens++;
            System.out.println(Thread.currentThread().getName() + " 归还令牌，剩余: " + tokens);

            // [问题点 C]：只唤醒一个等待线程
            this.notify();
        }
    }
}
