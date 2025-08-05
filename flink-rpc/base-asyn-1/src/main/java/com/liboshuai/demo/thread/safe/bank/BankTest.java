package com.liboshuai.demo.thread.safe.bank;

import java.util.concurrent.TimeUnit;

public class BankTest {
    public static void main(String[] args) throws InterruptedException {
        // 账号开始余额为0
        Account account = new Account(0);
        // 用户一，每次存1元，存10000次
        User user1 = new User(account, 10, 10000);
        // 用户二，每次存2元，存10000次
        User user2 = new User(account, 20, 10000);
        new Thread(user1).start();
        new Thread(user2).start();
        System.out.println("主线程等待5秒，以便两个用户都完成存钱行为");
        TimeUnit.SECONDS.sleep(5);
        // 正确结果应该为300000，如果出现了线程安全问题，则会小于正确值
        System.out.printf("主线程最后获取到账户余额为: %d%n", account.getBalance());
    }
}
