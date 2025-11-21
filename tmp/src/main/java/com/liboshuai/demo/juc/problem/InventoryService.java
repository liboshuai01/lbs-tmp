package com.liboshuai.demo.juc.problem;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryService {

    // 使用 volatile 保证大家都能立刻看到最新的库存？
    // 或者，这里即使换成 AtomicInteger，代码依然有 Bug，请思考为什么。
    private final AtomicInteger stock = new AtomicInteger(1); // TODO: Integer 修改为 AtomicInteger

    /**
     * 扣减库存
     * @return true 扣减成功, false 库存不足
     */
    public boolean deduct() {
        // 步骤 1: 检查 (Check)
        while (true) {
            int currentStock = stock.get();
            if (currentStock > 0) {
                // 模拟一点点业务耗时（比如记录日志，或者判断用户等级）
                try { Thread.sleep(10); } catch (InterruptedException e) {}

                // 步骤 2: 行动 (Act)
                if (stock.compareAndSet(currentStock, --currentStock)) {
                    System.out.println(Thread.currentThread().getName() + " 扣减成功，剩余库存: " + stock);
                    return true;
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " 扣减失败，库存不足");
                return false;
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        InventoryService service = new InventoryService();

        // 模拟两个用户同时抢购最后一个商品
        int threadCount = 2;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                service.deduct();
                latch.countDown();
            }, "User-" + (i + 1)).start();
        }

        latch.await();
    }
}
