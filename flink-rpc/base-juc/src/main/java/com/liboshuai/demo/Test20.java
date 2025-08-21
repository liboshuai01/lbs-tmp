package com.liboshuai.demo;

import java.util.concurrent.locks.LockSupport;

public class Test20 {

    private static volatile int flag = 1;

    public static void main(String[] args) throws InterruptedException {
        Task task1 = new Task(5, 1, 2, "一");
        Task task2 = new Task(5, 2, 3, "二");
        Task task3 = new Task(5, 3, 1, "三");
        Thread t1 = new Thread(task1, "t1");
        Thread t2 = new Thread(task2, "t2");
        Thread t3 = new Thread(task3, "t3");
        task1.setNextThread(t2);
        task2.setNextThread(t3);
        task3.setNextThread(t1);
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    static class Task implements Runnable{
        private final int loop;
        private final int current;
        private final int next;
        private final String message;
        private Thread nextThread;

        Task(int loop, int current, int next, String message) {
            this.loop = loop;
            this.current = current;
            this.next = next;
            this.message = message;
        }

        public void setNextThread(Thread nextThread) {
            this.nextThread = nextThread;
        }

        @Override
        public void run() {
            for (int i = 0; i < loop; i++) {
                while (flag != current) {
                    LockSupport.park();
                }
                System.out.printf(message);
                flag = next;
                LockSupport.unpark(nextThread);
            }
        }
    }
}
