package com.liboshuai.demo.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Test16 {
    public static void main(String[] args) throws InterruptedException {
        Account a = new Account("A", 1000);
        Account b = new Account("B", 1000);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                a.transfer(b, 100);
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                b.transfer(a, 200);
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t1.join();

    }

    static class Account {
        private final String name;
        private int money;
        private final ReentrantLock lock = new ReentrantLock();

        public Account(String name, int money) {
            this.name = name;
            this.money = money;
        }

        public String getName() {
            return name;
        }

        public int getMoney() {
            return money;
        }

        public void transfer(Account target, int amount) {
            System.out.printf("线程 %s: 尝试从 %s 转账 %d 到 %s%n",
                    Thread.currentThread().getName(), this.getName(), amount, target.getName());
            boolean success = false;
            try {
                if (this.lock.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        if (target.lock.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                if (this.money >= amount) {
                                    this.money -= amount;
                                    target.money += amount;
                                    success = true;
                                    System.out.printf("✅ 转账成功: %s -> %s, 金额: %d. 剩余: %s=%d, %s=%d%n",
                                            this.getName(), target.getName(), amount, this.getName(), this.getMoney(), target.getName(), target.getMoney());
                                }  else {
                                    System.out.printf("❌ 转账失败: %s 余额不足%n", this.getName());
                                }
                            } finally {
                                target.lock.unlock();
                            }
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("转账线程被中断");
            }

            if (!success) {
                System.out.printf("⚠️ 线程 %s: 未能获取所有锁或转账失败，放弃本次操作%n", Thread.currentThread().getName());
            }
        }
    }
}
