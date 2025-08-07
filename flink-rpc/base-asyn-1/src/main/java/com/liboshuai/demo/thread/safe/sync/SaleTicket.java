package com.liboshuai.demo.thread.safe.sync;

import java.util.concurrent.TimeUnit;

public class SaleTicket {
    public static void main(String[] args) throws InterruptedException {
        Ticket ticket = new Ticket(50);
        Thread t1 = new Thread(new SaleTask(ticket), "线程1");
        Thread t2 = new Thread(new SaleTask(ticket), "线程2");
        Thread t3 = new Thread(new SaleTask(ticket), "线程3");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }
}

class Ticket {

    private int saleSize;

    public Ticket(int saleSize) {
        this.saleSize = saleSize;
    }

    public synchronized void sale() {
        if (saleSize > 0) {
            System.out.printf("[%s] 卖出了一张票，现在剩余 [%d] 张票。%n", Thread.currentThread().getName(), saleSize);
            saleSize--;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

class SaleTask implements Runnable {

    private Ticket ticket;

    public SaleTask(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            ticket.sale();
        }
    }

}