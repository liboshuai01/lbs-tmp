package com.liboshuai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test09 {
    public static void main(String[] args) throws InterruptedException {
        TicketWindow ticketWindow = new TicketWindow(2000);
        List<Thread> threads = new ArrayList<>();
        AtomicInteger ticketCount = new AtomicInteger();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                int sell = ticketWindow.sell(randomAmount());
                ticketCount.getAndAdd(sell);
            }, "t" + i);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("卖出的票数: " + ticketCount.get());
        System.out.println("剩余的票数: " + ticketWindow.getCount());
    }

    // Random 为线程安全
    static Random random = new Random();

    // 随机 1~5
    public static int randomAmount() {
        return random.nextInt(5) + 1;
    }

    static class TicketWindow {
        private AtomicInteger count;

        public TicketWindow(int count) {
            this.count = new AtomicInteger(count);
        }

        public int getCount() {
            return count.get();
        }

        public int sell(int amount) {
            int oldCount = count.getAndAdd(-amount);
            if (oldCount >= amount) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return amount;
            } else {
                return 0;
            }
        }
    }
}
