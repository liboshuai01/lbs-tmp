package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class HotReloadConfig {

    // 核心配置内容，初始为 "default"
    private String config = "default";

    private final Object lock = new Object();

    /**
     * 模拟远程配置中心推送新配置
     */
    public void reload(String newConfig) {
        // 锁住当前的 config 对象，防止并发修改
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " 正在更新配置...");
            try {
                // 模拟网络或IO耗时
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.config = newConfig;
            System.out.println(Thread.currentThread().getName() + " 配置更新完成: " + this.config);
        }
    }

    /**
     * 业务线程读取配置进行处理
     */
    public void service() {
        // 锁住 config，保证读取期间不被修改
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " 获取锁, 当前配置: " + config);
            // 模拟业务逻辑处理
        }
    }
}
