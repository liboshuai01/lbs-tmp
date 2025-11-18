package com.liboshuai.demo.juc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JobMasterStartup {

    // 也就是我们需要等待 3 个组件初始化完成
    private static final int COMPONENT_COUNT = 3;

    // TODO: 定义 CountDownLatch
    private final CountDownLatch latch = new CountDownLatch(COMPONENT_COUNT);

    /**
     * 模拟某个组件初始化完成
     * @param componentName 组件名称
     */
    public void componentFinished(String componentName) {
        System.out.println(componentName + " is ready.");
        // TODO: 如何通知门闩“有一个完成了”？
        latch.countDown();
    }

    /**
     * 主线程调用：等待所有组件就绪
     * @param timeoutSeconds 最大等待时间
     * @return 是否成功（true=全员就绪，false=超时）
     */
    public boolean waitForComponents(long timeoutSeconds) throws InterruptedException {
        System.out.println("JobMaster is waiting for components...");

        // TODO: 核心逻辑
        // 1. 阻塞等待 latch 归零
        // 2. 支持超时控制
        // 3. 返回结果：如果 latch 归零了返回 true，如果超时了返回 false
        return latch.await(timeoutSeconds, TimeUnit.SECONDS);
//        return false; // 占位
    }

    // --- 辅助测试方法 ---
    public static void main(String[] args) throws InterruptedException {
        JobMasterStartup startup = new JobMasterStartup();

        // 模拟 3 个线程去初始化组件
        new Thread(() -> {
            sleep(1000); startup.componentFinished("ResourceManager");
        }).start();

        new Thread(() -> {
            sleep(2000); startup.componentFinished("SlotPool");
        }).start();

        new Thread(() -> {
            sleep(1500); startup.componentFinished("CheckpointCoordinator");
        }).start();

        boolean success = startup.waitForComponents(5);
        System.out.println("Startup Success: " + success);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}