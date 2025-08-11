package com.liboshuai.demo.interrupted;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrimeGenerator1 implements Runnable{

    private static final AtomicBoolean isCancelled = new AtomicBoolean(false);

    @Override
    public void run() {
        BigInteger p = BigInteger.ONE;
        while (!isCancelled.get()) {
            p = p.nextProbablePrime(); // 这是一个耗时的计算
            System.out.println("Generated Prime: " + p);
        }
        // 循环结束后，表示收到了中断请求
        System.out.println("Prime generator has been interrupted. Cleaning up and exiting.");
        // 在这里可以执行一些资源清理工作
    }

    public static void main(String[] args) {
        PrimeGenerator1 generator = new PrimeGenerator1();
        Thread thread = new Thread(generator);
        thread.start();

        try {
            // 让素数生成器运行 1 秒钟
            System.out.println("Main: Letting the prime generator run for 1 second...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Main: Interrupting the prime generator now.");
            isCancelled.set(true);
        }
    }
}
