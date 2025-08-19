package com.liboshuai.demo;

import java.util.concurrent.TimeUnit;

public class Test01 {

    static class App {
        public void start() {

            final Thread currentThread = Thread.currentThread();
            while (true) {
                if (currentThread.isInterrupted()) {
                    System.out.println("料理后事......");
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("执行耗时操作......");
                } catch (InterruptedException e) {
                    currentThread.interrupt();
                }
            }
        }

        public void stop(Thread thread) {
            thread.interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        Thread thread = new Thread(app::start);
        thread.start();
        TimeUnit.SECONDS.sleep(3);
        app.stop(thread);
    }
}
