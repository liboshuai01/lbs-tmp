package com.liboshuai.demo.thread.safe.sync;

public class NotSafeSaleTicket {
    public static void main(String[] args) {
        Ticket ticket = new Ticket(30);
        SaleTask task = new SaleTask(ticket);
        new Thread(task, "线程1").start();
        new Thread(task, "线程2").start();
        new Thread(task, "线程3").start();
    }

    static class Ticket {
        private int ticketCount;

        public Ticket(int ticketCount) {
            this.ticketCount = ticketCount;
        }

        public boolean sale() {
            if (ticketCount > 0) {
                System.out.println(Thread.currentThread().getName() + " 卖了第 " + ticketCount + " 张票");
                ticketCount--;
                return true;
            } else {
                return false;
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
