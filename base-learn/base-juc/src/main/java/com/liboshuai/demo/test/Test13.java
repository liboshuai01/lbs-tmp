package com.liboshuai.demo.test;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test13 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        MessageQueue messageQueue = new MessageQueue(4);
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

        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void put(Message message) {
            while (list.size() >= capacity) {
                try {
                    System.out.println("消息队列已满，等待消费......");
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            list.add(message);
            this.notifyAll();
        }

        public synchronized Message take() {
            while (list.isEmpty()) {
                try {
                    System.out.println("消息队列已空，等待生产......");
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Message message = list.removeFirst();
            this.notifyAll();
            return message;
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
