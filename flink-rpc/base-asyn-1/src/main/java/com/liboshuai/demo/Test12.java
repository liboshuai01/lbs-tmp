package com.liboshuai.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test12 {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Person(i));
        }
        TimeUnit.SECONDS.sleep(2);
        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Postman(i, "内容-" + i));
        }
        threadPool.shutdown();
    }

    static class Person implements Runnable{

        private final int id;

        public Person(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            GuardedObject guardedObject = MailBoxes.createGuardedObject(id);
            System.out.printf("用户等待收取id为[%d]的信件......%n", id);
            Object response = guardedObject.get(3000);
            System.out.printf("用户收到信件id为: [%d]，内容为：[%s]%n", id, response);
        }

    }

    static class Postman implements Runnable{

        private final int id;
        private final String mail;

        public Postman(int id, String mail) {
            this.id = id;
            this.mail = mail;
        }

        @Override
        public void run() {
            GuardedObject guardedObject = MailBoxes.getGuardedObject(id);
            guardedObject.completed(mail);
            System.out.printf("快递员送到信件id为：[%d]，内容为：[%s]%n", id, mail);
        }

    }

    static class MailBoxes {
        private static final Map<Integer, GuardedObject> map = new ConcurrentHashMap<>();

        public static GuardedObject createGuardedObject(int id) {
            GuardedObject guardedObject = new GuardedObject();
            map.put(id, guardedObject);
            return guardedObject;
        }

        public static GuardedObject getGuardedObject(int id) {
            return map.remove(id);
        }
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
            synchronized(this) {
                long startTime = System.currentTimeMillis();
                long waitTime = timeout;
                while (response == null) {
                    if (waitTime <= 0) {
                        break;
                    }
                    try {
                        this.wait(timeout);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    waitTime = waitTime - (System.currentTimeMillis() - startTime);
                }
                return response;
            }
        }

        public void completed(Object response) {
            synchronized(this) {
                this.response = response;
                this.notifyAll();
            }
        }
    }
}
