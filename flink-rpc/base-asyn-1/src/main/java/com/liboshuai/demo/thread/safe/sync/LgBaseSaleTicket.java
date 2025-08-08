package com.liboshuai.demo.thread.safe.sync;

import java.util.concurrent.atomic.AtomicInteger;

public class LgBaseSaleTicket {
    public static void main(String[] args) {
        Ticket ticket = new Ticket(30);
        SaleTask task = new SaleTask(ticket);
        new Thread(task, "线程1").start();
        new Thread(task, "线程2").start();
        new Thread(task, "线程3").start();
    }

    static class Ticket {
        private final AtomicInteger ticketCount;

        public Ticket(int ticketCount) {
            this.ticketCount = new AtomicInteger(ticketCount);
        }

        public boolean sale() {
            boolean result = false;
            while (true) {
                int currentCount = ticketCount.get();
                if (currentCount > 0) {
                    if (ticketCount.compareAndSet(currentCount, currentCount - 1)) {
                        System.out.println(Thread.currentThread().getName() + " 卖了第 " + currentCount + " 张票");
                        result = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            return result;
        }
    }

    static class SaleTask implements Runnable {

        private final Ticket ticket;

        public SaleTask(Ticket ticket) {
            this.ticket = ticket;
        }

        @Override
        public void run() {
            while (true) {
                boolean result = ticket.sale();
                if (!result) {
                    System.out.println(Thread.currentThread().getName() + " 票已经售尽，结束买票！");
                    break;
                }
            }
        }
    }
}
