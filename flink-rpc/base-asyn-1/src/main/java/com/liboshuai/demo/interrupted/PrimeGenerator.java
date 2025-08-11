package com.liboshuai.demo.interrupted;

import java.math.BigInteger;

public class PrimeGenerator implements Runnable{
    @Override
    public void run() {
        BigInteger p = BigInteger.ONE;
        // isInterrupted() 只检查状态，不清除。非常适合用作循环的判断条件。
        while (!Thread.currentThread().isInterrupted()) {
            p = p.nextProbablePrime(); // 这是一个耗时的计算
            System.out.println("Generated Prime: " + p);
        }
        // 循环结束后，表示收到了中断请求
        System.out.println("Prime generator has been interrupted. Cleaning up and exiting.");
        // 在这里可以执行一些资源清理工作
    }

    public static void main(String[] args) {
        PrimeGenerator generator = new PrimeGenerator();
        Thread thread = new Thread(generator);
        thread.start();

        try {
            // 让素数生成器运行 1 秒钟
            System.out.println("Main: Letting the prime generator run for 1 second...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 1秒后，主线程向生成器线程发送中断请求
            System.out.println("Main: Interrupting the prime generator now.");
            thread.interrupt(); // <<<--- 关键点：请求中断
        }
    }
}
