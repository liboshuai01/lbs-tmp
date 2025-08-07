package com.liboshuai.demo.thread.safe.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 案例2：生产者&消费者
 * 生产者(Productor)将产品交给店员(Clerk)，而消费者(Customer)从店员处取走产品，店员一次只能持有
 * 固定数量的产品(比如:20），如果生产者试图生产更多的产品，店员会叫生产者停一下，如果店中有空位放产品
 * 了再通知生产者继续生产；如果店中没有产品了，店员会告诉消费者等一下，如果店中有产品了再通知消费者来
 * 取走产品。
 */
public class ProducerConsumerTest {
    public static void main(String[] args) throws InterruptedException {
        Factory factory = new Factory(20);
        Producer producer = new Producer(factory);
        Consumer consumer = new Consumer(factory);
        Thread t1 = new Thread(producer, "生产者");
        Thread t2 = new Thread(consumer, "消费者1");
        Thread t3 = new Thread(consumer, "消费者2");
        t1.start();
        t2.start();
        t3.start();
        System.out.println("主线程等待生产者消费者完成......");
        t1.join();
        t2.join();
        t3.join();
        System.out.println("主线程等待完毕......");
    }
}

/**
 * 工厂类提供生产、销售产品的能力，但是产品存能有限制
 */
class Factory {

    /**
     * 产品最大存量
     */
    private final int size;

    /**
     * 产品现有量
     */
    private final List<String> productList = new ArrayList<>();

    public Factory(int size) {
        this.size = size;
    }

    /**
     * 制造产品的方法
     */
    public synchronized void madeProduct() {
        // 使用 while 循环判断，防止虚假唤醒
        while (productList.size() >= size) { // 条件改为 "当仓库满或超量时等待"
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // 能够执行到这里，说明条件 productList.size() < size 必然成立
        productList.add("产品");
        System.out.printf("线程 [%s] 生产了第一个产品，现存产品数量为: %d%n", Thread.currentThread().getName(), productList.size());
        notifyAll();
    }

    /**
     * 销售产品的方法
     */
    public synchronized void sellProduct() {
        // 使用 while 循环判断，防止虚假唤醒
        while (productList.isEmpty()) { // 条件改为 "当仓库为空时等待"
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 能够执行到这里，说明条件 !productList.isEmpty() 必然成立
        productList.remove(0);
        System.out.printf("线程 [%s] 消费了第一个产品，现存产品数量为: %d%n", Thread.currentThread().getName(), productList.size());
        notifyAll();
    }
}

/**
 * 生产者线程，利用工厂一直不断生产产品
 */
class Producer implements Runnable{

    private final Factory factory;

    public Producer(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            factory.madeProduct();
        }
    }
}

/**
 * 消费者线程，从工厂源源不断购买产品
 */
class Consumer implements Runnable {

    private final Factory factory;

    public Consumer(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            factory.sellProduct();
        }
    }

}