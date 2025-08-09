package com.liboshuai.demo.thread.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 这是一个模拟数据记录的简单类，在 Flink 中可以类比为 Buffer 或序列化后的数据记录。
 */
class DataRecord {
    private final String payload;

    public DataRecord(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "payload='" + payload + '\'' +
                '}';
    }
}

/**
 * 模拟 Flink 的数据源或上游算子 (Producer)。
 * 它会持续生成数据并放入阻塞队列中。
 */
class DataSourceProducer implements Runnable {
    // 使用 ArrayBlockingQueue 作为有界缓冲区
    private final BlockingQueue<DataRecord> buffer;
    private final int id;
    private volatile boolean isRunning = true;

    public DataSourceProducer(int id, BlockingQueue<DataRecord> buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("数据源(Producer-" + id + ") 开始生成数据...");
        try {
            int counter = 0;
            while (isRunning) {
                DataRecord record = new DataRecord("Event-" + counter + " from Producer-" + id);

                // put() 方法是阻塞的。如果队列已满，它会一直等待直到队列中有可用空间。
                // 这完美地模拟了 Flink 的反压机制。
                System.out.println("Producer-" + id + " 准备放入数据: " + record + ". 当前队列大小: " + buffer.size());
                buffer.put(record);
                System.out.println("Producer-" + id + " 成功放入数据: " + record + ". 当前队列大小: " + buffer.size());

                counter++;
                // 模拟数据生成间隔
                TimeUnit.MILLISECONDS.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("数据源(Producer-" + id + ") 被中断。");
        }
        System.out.println("数据源(Producer-" + id + ") 停止。");
    }

    public void stop() {
        isRunning = false;
    }
}

/**
 * 模拟 Flink 的下游算子 (Consumer)。
 * 它会从阻塞队列中消费数据，并故意处理得很慢以触发反压。
 */
class OperatorConsumer implements Runnable {
    private final BlockingQueue<DataRecord> buffer;
    private volatile boolean isRunning = true;

    public OperatorConsumer(BlockingQueue<DataRecord> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("下游算子(Consumer) 开始处理数据...");
        try {
            while (isRunning || !buffer.isEmpty()) {
                // take() 方法是阻塞的。如果队列为空，它会一直等待直到队列中有数据可取。
                DataRecord record = buffer.take();

                System.out.println("Consumer 开始处理: " + record + ". 当前队列大小: " + buffer.size());

                // 模拟耗时的处理操作，比如复杂的计算或IO操作。
                // 这个延迟是触发反压的关键。
                TimeUnit.SECONDS.sleep(2);

                System.out.println("Consumer 完成处理: " + record + ".");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("下游算子(Consumer) 被中断。");
        }
        System.out.println("下游算子(Consumer) 停止。");
    }

    public void stop() {
        isRunning = false;
    }
}


/**
 * 主程序，用于启动和协调生产者和消费者。
 */
public class ArrayBlockingQueueDemo  {

    public static void main(String[] args) throws InterruptedException {
        // 创建一个容量为 5 的 ArrayBlockingQueue。
        // 这是一个有界的、FIFO（先进先出）的阻塞队列。
        // "true" 参数表示使用公平策略，等待时间最长的线程会优先获得访问权限。
        BlockingQueue<DataRecord> sharedBuffer = new ArrayBlockingQueue<>(5, true);

        // 创建生产者和消费者任务
        DataSourceProducer producer = new DataSourceProducer(1, sharedBuffer);
        OperatorConsumer consumer = new OperatorConsumer(sharedBuffer);

        // 启动生产者和消费者线程
        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

        // 运行一段时间后停止
        TimeUnit.SECONDS.sleep(20);

        System.out.println("\n=============================================");
        System.out.println("时间到，正在停止生产者/消费者...");
        System.out.println("=============================================\n");
        producer.stop();
        consumer.stop();

        // 等待生产者线程结束
        producerThread.join();
        System.out.println("生产者线程已停止。");

        // 等待消费者处理完队列中剩余的数据
        // consumer 的循环条件是 isRunning || !buffer.isEmpty()，所以它会自己结束
        consumerThread.join();
        System.out.println("消费者线程已停止，所有缓冲数据处理完毕。");
        System.out.println("最终队列大小: " + sharedBuffer.size());
    }
}