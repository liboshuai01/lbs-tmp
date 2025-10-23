package com.liboshuai.demo.actor;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 演示使用传统锁 (ReentrantLock) 来管理并发库存。
 * * 业务场景：模拟一个高并发的电商库存系统。
 * 假设一个商品，初始库存为 100 件。
 * 我们模拟 150 个并发用户同时抢购，每个用户购买 1 件。
 *
 * 预期结果：
 * - 100 个用户购买成功。
 * - 50 个用户购买失败（因为库存不足）。
 * - 最终库存为 0。
 *
 * 如果没有锁，多个线程会同时读取到"有库存"的状态，然后各自扣减，导致库存变为负数（超卖）。
 */
public class TraditionalLockDemo {

    public static void main(String[] args) {
        // 1. 设置初始库存为 100
        Inventory inventory = new Inventory(100);

        // 2. 创建一个固定大小的线程池，模拟并发用户
        ExecutorService executor = Executors.newFixedThreadPool(10);

        int numberOfCustomers = 150; // 模拟150个顾客
        int purchaseAmountPerCustomer = 1; // 每个顾客购买1件

        System.out.println("--- 并发抢购模拟开始 ---");
        System.out.println("初始库存: " + inventory.getStock());
        System.out.println("模拟并发用户数: " + numberOfCustomers);
        System.out.println("每个用户尝试购买: " + purchaseAmountPerCustomer + "件");
        System.out.println("---------------------------------");


        // 3. 提交 150 个购买任务到线程池
        for (int i = 0; i < numberOfCustomers; i++) {
            executor.submit(new PurchaseTask(inventory, purchaseAmountPerCustomer, "顾客-" + (i + 1)));
        }

        // 4. 关闭线程池并等待所有任务完成
        executor.shutdown();
        try {
            // 等待最多1分钟，让所有任务执行完毕
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow(); // 如果超时则强制关闭
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("---------------------------------");
        System.out.println("--- 并发抢购模拟结束 ---");
        System.out.println("最终剩余库存: " + inventory.getStock());

        // 5. 验证结果
        if (inventory.getProtectedStock() == 0) {
            System.out.println("结果正确: 库存已售罄 (100件已售出, 50次尝试失败)。");
        } else {
            System.out.println("结果错误: 最终库存不为0! (库存 " + inventory.getProtectedStock() + ")");
        }
    }
}

/**
 * 商品库存类 (共享资源)
 * * 这是一个“资源类”，它持有的 'stock' 变量是多线程共享和竞争的资源。
 * 我们使用 ReentrantLock 来保护 'stock' 变量的 "读取-修改-写入" 操作，确保其原子性。
 */
class Inventory {
    private int stock;

    // 1. 定义一个锁
    // ReentrantLock 是一个可重入的互斥锁，功能比 synchronized 关键字更灵活
    private final Lock lock = new ReentrantLock();

    public Inventory(int initialStock) {
        this.stock = initialStock;
    }

    /**
     * 尝试购买商品（线程安全的方法）
     * * @param amount 购买数量
     * @param customerName 顾客名称
     * @return true 如果购买成功, false 如果库存不足
     */
    public boolean purchase(int amount, String customerName) {

        // 2. 在访问共享资源前加锁
        // lock() 会阻塞，直到获取到锁
        lock.lock();

        try {
            String threadName = Thread.currentThread().getName();
            System.out.println(customerName + " (线程 " + threadName + ") 尝试购买 " + amount + "件... " + "当前库存: " + stock);

            // 检查库存
            if (stock >= amount) {
                // 模拟处理订单所需的时间（例如：验证、扣款等）
                // 这短暂的延迟会极大地增加 "没有锁时" 发生并发问题的几率
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // 关键的 "Read-Modify-Write" 原子操作
                stock -= amount;

                System.out.println("-> " + customerName + " (线程 " + threadName + ") 购买成功! " + "剩余库存: " + stock);
                return true;
            } else {
                System.out.println("-> " + customerName + " (线程 " + threadName + ") 购买失败! " + "库存不足 (仅剩 " + stock + "件).");
                return false;
            }
        } finally {
            // 3. 必须在 finally 块中释放锁，确保即使发生异常锁也会被释放
            lock.unlock();
        }
    }

    /**
     * 获取当前库存（用于非事务性查看，可能不是100%最新）
     */
    public int getStock() {
        return stock;
    }

    /**
     * 获取当前库存（线程安全，用于最后验证）
     */
    public int getProtectedStock() {
        lock.lock();
        try {
            return stock;
        } finally {
            lock.unlock();
        }
    }
}

/**
 * 模拟一个顾客的购买任务
 * * 实现了 Runnable 接口，可以被线程池执行。
 */
class PurchaseTask implements Runnable {
    private final Inventory inventory;
    private final int amountToPurchase;
    private final String customerName;

    public PurchaseTask(Inventory inventory, int amountToPurchase, String customerName) {
        this.inventory = inventory;
        this.amountToPurchase = amountToPurchase;
        this.customerName = customerName;
    }

    @Override
    public void run() {
        // 每个任务执行一次购买尝试
        inventory.purchase(amountToPurchase, customerName);
    }
}
