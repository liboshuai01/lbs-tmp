package com.liboshuai.demo.juc.problem;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 第二关：死锁分析与解决
 * 模拟两个算子之间交换资源导致的死锁
 */
public class ConcurrencyTask02 {

    private static final Random random = new Random();

    public static void main(String[] args) {
        // 创建两个资源账户
        Account acc1 = new Account(1, 1000);
        Account acc2 = new Account(2, 1000);

        // 线程1: 从 acc1 转给 acc2
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                transfer(acc1, acc2, 1);
            }
        }, "Thread-A->B");

        // 线程2: 从 acc2 转给 acc1
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                transfer(acc2, acc1, 1);
            }
        }, "Thread-B->A");

        t1.start();
        t2.start();
    }

    /**
     * 模拟转账操作：从 from 账户扣钱，给 to 账户加钱
     * 必须同时持有两个账户的锁，才能保证原子性
     */
    private static void transfer(Account from, Account to, int amount) {
        // TODO: 任务 B - 修改这里的加锁逻辑，防止死锁
        // 当前逻辑：先锁 'from'，再锁 'to'。
        // 如果 A->B 和 B->A 同时发生，就会死锁。

        ReentrantLock fromLock = from.lock;
        ReentrantLock toLock = to.lock ;

        // --- 原始的死锁代码 (请修改这部分) ---
        while (true) {
            if (fromLock.tryLock()) {
                try {
                    // 模拟处理耗时，增加死锁概率
                    sleep(1);
                    if (toLock.tryLock()) {
                        try {
                            if (from.balance >= amount) {
                                from.balance -= amount;
                                to.balance += amount;
                                System.out.println(Thread.currentThread().getName() +
                                        " 转账成功: " + from.id + " -> " + to.id);
                            }
                            return;
                        } finally {
                            toLock.unlock();
                        }
                    }
                } finally {
                    fromLock.unlock();
                }
            }
            // TODO: 进行随机退避
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        // ------------------------------------
    }

    // 辅助方法：休眠
    private static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { }
    }

    // 资源账户类
    static class Account {

        ReentrantLock lock = new ReentrantLock();

        final int id;
        int balance;

        public Account(int id, int balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    /*
     * TODO: 思考题回答
     * 除了顺序加锁，使用 ReentrantLock 的什么方法可以避免死锁？
     * 答：lock.tryLock()
     */
}
