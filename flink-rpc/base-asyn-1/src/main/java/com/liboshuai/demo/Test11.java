package com.liboshuai.demo;

import java.util.concurrent.TimeUnit;

public class Test11 {
    public static void main(String[] args) throws InterruptedException {
        GuardedObject guardedObject = new GuardedObject();

        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "开始获取响应值...");
//            Object response = guardedObject.get();
            Object response = guardedObject.get(3000);
            System.out.println(Thread.currentThread().getName() + "得到了响应值: " + response);
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String response = "我就是结果，服不服！";
            guardedObject.completed(response);
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    static class GuardedObject {
        private Object response;

        public Object get() {
            synchronized (this) {
                while (response == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
                return response;
            }
        }

        public Object get(long timeout) {
            synchronized (this) {
                long startTime = System.currentTimeMillis();
                long waitTime = timeout;
                while (response == null) {
                    if (waitTime <= 0) {
                        break;
                    }
                    try {
                        this.wait(waitTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    waitTime = waitTime - elapsedTime;
                }
                return response;
            }
        }

        public void completed(Object response) {
            synchronized (this) {
                this.response = response;
                this.notifyAll();
            }
        }
    }
}
