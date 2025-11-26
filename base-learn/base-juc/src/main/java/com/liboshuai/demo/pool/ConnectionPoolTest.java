package com.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 压力测试：模拟电商大促场景
 * 场景：
 * - 连接池大小: 5 (资源非常紧缺)
 * - 并发用户数: 20 (并发度 4:1)
 * - 用户忍耐时间: 2秒 (超过2秒没拿到连接就报错)
 */
@Slf4j
public class ConnectionPoolTest {

    public static void main(String[] args) throws InterruptedException {
        // 1. 初始化一个小池子
        MiniConnectionPool dbPool = new MiniConnectionPool(5);

        // 模拟 20 个并发请求
        int userCount = 20;

        // JUC工具: CountDownLatch (倒计时门闩)
        // 用来让主线程等待所有子线程跑完，比 Thread.sleep(10000) 更科学
        CountDownLatch latch = new CountDownLatch(userCount);

        // 统计数据
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        log.info("=== 🚀 双11大促开始，{} 个用户并发涌入 ===", userCount);

        for (int i = 1; i <= userCount; i++) {
            int userId = i;
            new Thread(() -> {
                MockConnection conn = null;
                try {
                    // 模拟用户点击下单，稍有先后
                    Thread.sleep((long) (Math.random() * 200));

                    log.info("用户[{}] 尝试获取连接...", userId);

                    // 2. 尝试借连接 (超时时间 2秒)
                    conn = dbPool.borrow(2, TimeUnit.SECONDS);

                    if (conn != null) {
                        // --- 拿到连接，执行业务 ---
                        conn.executeQuery("UPDATE orders SET status=1 WHERE user_id=" + userId);

                        // 模拟业务处理耗时 (持有连接 0.5秒)
                        // 这个时间越长，其他人等待越久，越容易超时
                        Thread.sleep(500);

                        successCount.incrementAndGet();
                    } else {
                        // --- 没拿到连接 (超时) ---
                        log.error("❌ 用户[{}] 系统繁忙，请求被熔断 (获取连接超时)", userId);
                        failCount.incrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 3. 【至关重要】必须在 finally 块中归还连接
                    // 否则连接泄露，池子很快就空了，所有人都得死锁
                    if (conn != null) {
                        dbPool.returnConnection(conn);
                    }
                    // 完成一个，门闩减一
                    latch.countDown();
                }
            }, "User-" + userId).start();
        }

        // 主线程在这里阻塞，直到 count 减为 0
        latch.await();

        log.info("=== 🏁 大促结束，统计结果 ===");
        log.info("✅ 成功交易: {}", successCount.get());
        log.info("❌ 失败(超时): {}", failCount.get());
        log.info("📦 剩余空闲连接: {}", dbPool.getIdleCount());

        // 验证连接是否都有借有还 (如果不等于 5，说明代码有 Bug)
        if (dbPool.getIdleCount() != 5) {
            log.error("⚠️ 警告：发生连接泄露！预计剩余 5，实际剩余 {}", dbPool.getIdleCount());
        }

        dbPool.shutdown();
    }
}