package com.liboshuai.demo.safe.bank;

/**
 * 用户类
 */
public class User implements Runnable{
    /**
     * 注意多个用户共同往一个账号为存钱
     */
    private final Account account;

    /**
     * 存钱数目
     */
    private final int amount;

    /**
     * 存钱次数
     */
    private final int count;

    public User(Account account, int amount, int count) {
        this.account = account;
        this.amount = amount;
        this.count = count;
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            account.deposit(amount);
        }
    }
}
