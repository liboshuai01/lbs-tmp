package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class StopThreadDemo {

    // 控制任务停止的标志位
    private static boolean stopRequested = false;

    public static void main(String[] args) throws InterruptedException {

        // 启动工作线程
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            // 🛑 这里的检测逻辑
            while (!stopRequested) {
                i++;
                // 这里没有任何 synchronized 或 I/O 操作，纯 CPU 计算
            }
            System.out.println("工作线程响应停止，最终 i = " + i);
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);

        // 主线程发出停止信号
        stopRequested = true;
        System.out.println("主线程已发出停止信号！");
    }
}
