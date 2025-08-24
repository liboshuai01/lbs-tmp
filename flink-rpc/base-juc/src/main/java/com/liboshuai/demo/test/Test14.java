package com.liboshuai.demo.test;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Test14 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        MessageQueue messageQueue = new MessageQueue(6);
        threadPool.execute(new Producer(messageQueue));
        threadPool.execute(new Consumer(messageQueue));
        threadPool.execute(new Consumer(messageQueue));
        threadPool.shutdown();
    }

    static class Consumer implements Runnable{

        private final MessageQueue messageQueue;

        public Consumer(MessageQueue messageQueue) {
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Message message = messageQueue.take();
                System.out.printf("线程 [%s] 获取到信息 [%s]%n", Thread.currentThread().getName(), message);
            }
        }

    }

    static class Producer implements Runnable {

        private final AtomicInteger id = new AtomicInteger();

        private final MessageQueue messageQueue;

        public Producer(MessageQueue messageQueue) {
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Message message = new Message(id.incrementAndGet(), UUID.randomUUID().toString());
                messageQueue.put(message);
                System.out.printf("线程 [%s] 存放了信息 [%s]%n", Thread.currentThread().getName(), message);
            }
        }

    }

    static class MessageQueue {
        private final int capacity;
        private final LinkedList<Message> list = new LinkedList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        public void put(Message message) {
            lock.lock();
            try {
                while (list.size() >= capacity) {
                    System.out.println("消息队列已满，等待消费......");
                    notFull.await();
                }
                list.add(message);
                notEmpty.signalAll();
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        public Message take() {
            lock.lock();
            try {
                while (list.isEmpty()) {
                    System.out.println("消息队列已空，等待生产......");
                    notEmpty.await();
                }
                Message message = list.removeFirst();
                notFull.signalAll();
                return message;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                lock.unlock();
            }
        }
    }

    static class Message {
        private final int id;
        private final String content;

        public Message(int id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            Message message = (Message) o;
            return id == message.id && Objects.equals(content, message.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, content);
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
