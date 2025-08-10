package com.liboshuai.demo.safe.sync;

import java.util.concurrent.atomic.AtomicStampedReference;

public class LgVersionSaleTicket {
    public static void main(String[] args) {
        Ticket ticket = new Ticket(30);
        SaleTask task = new SaleTask(ticket);
        new Thread(task, "线程1").start();
        new Thread(task, "线程2").start();
        new Thread(task, "线程3").start();
    }

    static class Ticket {
        // 修改点: 使用 AtomicStampedReference 来同时持有票数和版本号（stamp）
        // Integer 是值（票数），构造函数的第二个参数是初始版本号
        private final AtomicStampedReference<Integer> ticketCountRef;

        public Ticket(int ticketCount) {
            // 初始票数为 'ticketCount', 初始版本号为 0
            this.ticketCountRef = new AtomicStampedReference<>(ticketCount, 0);
        }

        public boolean sale() {
            // 使用循环来实现“比较并交换”（CAS）操作
            // 如果CAS操作失败（因为其他线程卖出了一张票），循环会重新尝试
            while (true) {
                // 1. 获取当前版本号（stamp）和当前的票数
                int[] stampHolder = new int[1]; // 用于获取版本号的容器
                Integer currentCount = ticketCountRef.get(stampHolder);
                int currentStamp = stampHolder[0];

                // 2. 检查是否还有余票
                if (currentCount > 0) {
                    // 3. 尝试原子性地更新票数和版本号
                    // 这个操作只有在 currentCount 和 currentStamp 自我们读取以来没有被改变过时才会成功
                    boolean success = ticketCountRef.compareAndSet(
                            currentCount,      // expectedReference: 我们预期的票数值
                            currentCount - 1,  // newReference: 新的票数值
                            currentStamp,      // expectedStamp: 我们预期的版本号
                            currentStamp + 1   // newStamp: 新的版本号
                    );

                    // 如果更新成功，打印信息并退出方法
                    if (success) {
                        System.out.println(Thread.currentThread().getName() + " 卖了第 " + currentCount + " 张票, 当前版本号: " + (currentStamp + 1));
                        return true;
                    }
                    // 如果 'success' 为 false，意味着另一个线程已经卖出了一张票
                    // while 循环会使其重新开始这个过程

                } else {
                    // 没有余票了，退出循环并返回 false
                    return false;
                }
            }
        }
    }

    static class SaleTask implements Runnable {
        private final Ticket ticket;

        public SaleTask(Ticket ticket) {
            this.ticket = ticket;
        }

        @Override
        public void run() {
            // 这里的逻辑保持不变
            while (true) {
                // 复杂的CAS逻辑现在被封装在 ticket.sale() 方法内部了
                boolean result = ticket.sale();
                if (!result) {
                    System.out.println(Thread.currentThread().getName() + " 票已经售尽，结束买票！");
                    break;
                }
            }
        }
    }
}
