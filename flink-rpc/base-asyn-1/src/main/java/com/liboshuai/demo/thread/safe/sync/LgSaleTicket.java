package com.liboshuai.demo.thread.safe.sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LgSaleTicket {
    public static void main(String[] args) throws InterruptedException {
        LockTicket lockTicket = new LockTicket(50);
        LockSaleTask lockSaleTask = new LockSaleTask(lockTicket);
        Thread t1 = new Thread(lockSaleTask, "线程1");
        Thread t2 = new Thread(lockSaleTask, "线程2");
        Thread t3 = new Thread(lockSaleTask, "线程3");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    static class LockTicket {
        private final AtomicInteger ticketSize;

        public LockTicket(int ticketSize) {
            this.ticketSize = new AtomicInteger(ticketSize);
        }

        public boolean sale() {
            boolean result = false;
            // 使用循环来实现CAS自旋
            while (true) {
                // 1. 获取当前票数
                int currentTickets = ticketSize.get();
                // 2. 判断是否还有余票
                if (currentTickets > 0) {
                    // 模拟网络延迟或业务耗时
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 3. 尝试使用 CAS 更新票数
                    // 只有当【当前票数】仍然是我们获取到的 currentTickets 时，才将其更为 currentTickets - 1
                    if (ticketSize.compareAndSet(currentTickets, currentTickets - 1)) {
                        System.out.printf("[%s] 卖出了一张票，现在剩余 [%d] 张票。%n", Thread.currentThread().getName(), currentTickets - 1);
                        // 更新成功，跳出循环
                        result = true;
                        break;
                    }
                } else {
                    System.out.printf("[%s] 发现没有票了%n", Thread.currentThread().getName());
                    break;
                }
            }
            return result;
        }
    }

    static class LockSaleTask implements Runnable {

        private final LockTicket lockTicket;

        public LockSaleTask(LockTicket lockTicket) {
            this.lockTicket = lockTicket;
        }

        @Override
        public void run() {
            while (true) {
                boolean result = lockTicket.sale();
                if (!result) {
                    System.out.println(Thread.currentThread().getName() + " 票已经售尽，结束买票！");
                    break;
                }
            }
        }

    }

}

