package com.liboshuai.demo.thread.juc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreDemo {
    public static void main(String[] args) {
        int appCount = 10;
        ConnectPool connectPool = new ConnectPool(3);
        ExecutorService threadPool = Executors.newFixedThreadPool(appCount);
        for (int i = 0; i < appCount; i++) {
            threadPool.execute(new SemaphoreTask(connectPool));
        }
        threadPool.shutdown();
    }
}

class ConnectPool{

    private final Semaphore semaphore;

    public ConnectPool(int maxConnectNum) {
        this.semaphore = new Semaphore(maxConnectNum,true);
    }

    public void getConnect() {
        try {
            System.out.printf("线程 [%s] 等待获取数据库连接，现连接池资源数量为: [%d]%n", Thread.currentThread().getName(), semaphore.availablePermits());
            semaphore.acquire();
            System.out.printf("线程 [%s] 已经获取到数据库连接，现连接池资源数量为: [%d]%n", Thread.currentThread().getName(), semaphore.availablePermits());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnect() {
        semaphore.release();
        System.out.printf("线程 [%s] 归还了数据库连接，现连接池资源数量为: [%d]%n", Thread.currentThread().getName(), semaphore.availablePermits());
    }
}

class SemaphoreTask implements Runnable{

    private final ConnectPool connectPool;

    public SemaphoreTask(ConnectPool connectPool) {
        this.connectPool = connectPool;
    }

    @Override
    public void run() {
        try {
            connectPool.getConnect();
            System.out.printf("线程 [%s] 获取到连接资源后开始自己的业务处理...%n", Thread.currentThread().getName());
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 5000 + 1000));
            System.out.printf("线程 [%s] 自己的业务处理完毕了......%n", Thread.currentThread().getName());
        } catch (InterruptedException e) {
            System.err.printf("线程 [%s] 获取连接资源出现异常！%n", Thread.currentThread().getName());
        } finally {
            connectPool.closeConnect();
        }
    }
}