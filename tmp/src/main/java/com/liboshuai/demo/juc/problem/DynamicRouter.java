package com.liboshuai.demo.juc.problem;


import java.util.concurrent.TimeUnit;

public class DynamicRouter {

    // 核心配置：下游服务的 IP 地址 (初始值)
    private String targetIp = "192.168.0.1";
    private final Object lock = new Object();

    /**
     * 运维操作：热更新路由配置
     */
    public void updateRoute(String newIp) {
        // 🔒 锁住当前的 IP 对象，防止并发修改
        synchronized (lock) {
            System.out.println("【运维】" + Thread.currentThread().getName() + " 获取锁，开始更新...");

            // 模拟网络延时或复杂的配置校验耗时
            try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}

            this.targetIp = newIp;

            System.out.println("【运维】" + Thread.currentThread().getName() + " 更新完成，新IP: " + this.targetIp);
        }
    }

    /**
     * 业务操作：处理并发请求
     */
    public void routeRequest() {
        // 🔒 锁住当前的 IP 对象，防止读到脏数据或在更新时读取
        synchronized (lock) {
            System.out.println("【业务】" + Thread.currentThread().getName() + " 获取锁，正在通过 IP: " + targetIp + " 转发请求");

            // 模拟业务处理耗时
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) {}
        }
    }

    // 测试入口（模拟场景）
    public static void main(String[] args) {
        DynamicRouter router = new DynamicRouter();

        // 1. 启动业务线程，不断处理请求
        new Thread(() -> {
            while (true) {
                router.routeRequest();
                try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) {}
            }
        }, "Biz-Thread").start();

        // 2. 休眠一下，让业务跑起来
        try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) {}

        // 3. 启动运维线程，更新配置
        new Thread(() -> {
            router.updateRoute("10.0.0.1");
        }, "Ops-Thread").start();
    }
}
