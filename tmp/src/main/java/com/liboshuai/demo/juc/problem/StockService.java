package com.liboshuai.demo.juc.problem;

import java.util.concurrent.TimeUnit;

public class StockService {

    /**
     * 执行调拨：从 source 仓库移动 amount 库存到 target 仓库
     */
    public void transfer(Warehouse source, Warehouse target, int amount) {
        System.out.println("线程[" + Thread.currentThread().getName() + "] 准备执行调拨: " + source.id + " -> " + target.id);

        // 1. 锁住源仓库，防止别人同时扣减
        synchronized ((source.id.compareTo(target.id) < 0) ? source : target) {
            System.out.println("线程[" + Thread.currentThread().getName() + "] 持有锁: " + source.id);

            // 模拟业务检查耗时（如检查库存是否充足）
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
            }

            // 2. 锁住目标仓库，防止别人同时增加
            synchronized ((source.id.compareTo(target.id) > 0) ? source : target) {
                System.out.println("线程[" + Thread.currentThread().getName() + "] 持有锁: " + target.id);

                if (source.stock < amount) {
                    throw new RuntimeException("库存不足");
                }

                // 3. 执行内存操作
                source.stock -= amount;
                target.stock += amount;

                System.out.println("调拨完成");
            }
        }
    }

    public static class Warehouse {
        public final String id;
        public int stock;

        public Warehouse(String id, int stock) {
            this.id = id;
            this.stock = stock;
        }
    }
}
