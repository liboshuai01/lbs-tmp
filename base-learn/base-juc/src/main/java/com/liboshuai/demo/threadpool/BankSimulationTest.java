package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 综合测试：银行大厅模拟
 * 场景：
 * - Core=2 (正式柜员)
 * - Queue=3 (等待座位)
 * - Max=5 (业务高峰开启临时窗口)
 * - 20 个客户并发到达
 */
@Slf4j
public class BankSimulationTest {

    public static void main(String[] args) throws InterruptedException {
        // 1. 初始化银行 (线程池)
        // 拒绝策略使用 DiscardPolicy (人太多直接不让进，且不报错)
        MyThreadPool bank = new MyThreadPool(
                2, 5, 2, TimeUnit.SECONDS, 3,
                new MyRejectPolicy.DiscardPolicy()
        );

        log.info("=== 🏦 银行开门，正式柜员准备就绪 ===");

        // 2. 启动监控线程 (后台观察线程池状态)
        Thread monitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    // 获取内部状态
                    int poolSize = bank.getWorkerCount();
                    int queueSize = bank.getQueue().size();
                    log.info("📊 [监控] 柜员数(线程): {}, 等待人数(队列): {}", poolSize, queueSize);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitor.setDaemon(true); // 守护线程
        monitor.start();

        // 3. 模拟 20 个客户涌入
        AtomicInteger successCount = new AtomicInteger(0);
        Random random = new Random();

        for (int i = 1; i <= 20; i++) {
            int customerId = i;
            // 稍微错开一点到达时间，模拟真实并发
            Thread.sleep(50);

            try {
                bank.execute(() -> {
                    try {
                        // 模拟办理业务耗时 (0.5s - 2s)
                        int serviceTime = 500 + random.nextInt(1500);
                        log.info("👨‍💼 客户[{}] 开始办理业务 (预计耗时: {}ms)", customerId, serviceTime);
                        Thread.sleep(serviceTime);
                        log.info("✅ 客户[{}] 业务办理完成", customerId);
                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                // 如果是 AbortPolicy 会抛异常到这里
                log.warn("🚫 客户[{}] 被拒之门外", customerId);
            }
        }

        log.info("=== 🛑 客户进场完毕，不再接待新客 ===");

        // 4. 观察阶段
        // 此时应该能看到：
        // - 任务逐渐被消化，等待人数归零
        // - 超过 KeepAliveTime (2s) 后，柜员数从 5 降回 2 (临时工下班)
        Thread.sleep(8000);

        log.info("=== ⏸ 业务高峰期已过，准备打烊 ===");

        // 5. 关闭银行
        bank.shutdown();

        // 再次尝试提交验证 shutdown 效果
        try {
            bank.execute(() -> log.info("我是迟到的客户"));
        } catch (Exception e) {
            log.warn("打烊后的客户被拒绝: {}", e.getMessage());
        }

        log.info("=== 🏁 测试结束. 今日成功接待客户数: {} ===", successCount.get());
    }
}