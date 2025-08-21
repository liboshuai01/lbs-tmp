package com.liboshuai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Test19 {
    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer(3);
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
        private final ReentrantLock lock = new ReentrantLock();
        private final List<Condition> conditionList = new ArrayList<>();

        Printer(int threadCount) {
            for (int i = 0; i < threadCount; i++) {
                conditionList.add(lock.newCondition());
            }
        }

        public void print(int current, int next, String message) {
            lock.lock();
            try {
                while (flag != current) {
                    conditionList.get(current - 1).await();
                }
                System.out.printf(message);
                flag = next;
                conditionList.get(next - 1).signalAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
