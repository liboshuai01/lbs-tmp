package com.liboshuai.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test18 {
    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(new Task(5,1,2,"一",printer));
        threadPool.execute(new Task(5,2,3,"二",printer));
        threadPool.execute(new Task(5,3,1,"三",printer));
        threadPool.shutdown();
    }

    static class Task implements Runnable {

        private final int loop;
        private final int current;
        private final int next;
        private final String message;
        private final Printer printer;

        Task(int loop, int current, int next, String message, Printer printer) {
            this.loop = loop;
            this.current = current;
            this.next = next;
            this.message = message;
            this.printer = printer;
        }

        @Override
        public void run() {
            for (int i = 0; i < loop; i++) {
                printer.print(current,next,message);
            }
        }
    }

    static class Printer {

        private int flag = 1;

        public synchronized void print(int current, int next, String message) {
            while (flag != current) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.printf(message);
            flag = next;
            this.notifyAll();
        }
    }
}
