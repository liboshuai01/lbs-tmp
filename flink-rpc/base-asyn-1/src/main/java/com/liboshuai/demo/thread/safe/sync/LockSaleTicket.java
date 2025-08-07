package com.liboshuai.demo.thread.safe.sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LockSaleTicket {
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
}

class LockTicket {
    private int ticketSize;

    private final ReentrantLock lock = new ReentrantLock(true);

    public LockTicket(int ticketSize) {
        this.ticketSize = ticketSize;
    }

    public void sale() {
        lock.lock();
        try {
            if (ticketSize > 0) {
                System.out.printf("[%s] 卖出了一张票，现在剩余 [%d] 张票。%n", Thread.currentThread().getName(), ticketSize);
                ticketSize--;
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}

class LockSaleTask implements Runnable {

    private final LockTicket lockTicket;

    public LockSaleTask(LockTicket lockTicket) {
        this.lockTicket = lockTicket;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            lockTicket.sale();
        }
    }

}
