package com.liboshuai.demo.safe.bank;

/**
 * 账号类
 */
public class Account {

    private int balance;

    public Account(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    /**
     * 存款方法
     * @param amount 新存入的金额
     */
    public void deposit(int amount) {
        synchronized(this) {
            balance += amount;
            System.out.printf("线程 [%s] 存入了 [%d], 当前余额为 [%d]%n", Thread.currentThread().getName(), amount, balance);
        }
    }
}
