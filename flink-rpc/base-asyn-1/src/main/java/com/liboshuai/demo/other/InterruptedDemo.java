package com.liboshuai.demo.other;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Java 生产级别 InterruptedException 运用实战示例
 *
 * 该示例将应用主程序 (Application) 和消息消费者 (MessageConsumer) 整合到同一个文件中。
 * 使用静态内部类 MessageConsumer 来定义后台任务，结构清晰，便于演示。
 *
 * 场景：
 * 一个后台服务持续从消息队列消费数据并处理。
 * 该服务必须支持优雅关闭：在收到关闭信号时，能处理完当前任务，释放资源，然后干净地退出。
 */
public class InterruptedDemo {

    /**
     * 应用程序主入口，负责启动和关闭消费者线程。
     */
    public static void main(String[] args) {
        // 1. 创建并启动消费者任务线程
        MessageConsumer consumerTask = new MessageConsumer();
        Thread consumerThread = new Thread(consumerTask, "message-consumer-thread");
        consumerThread.start();

        System.out.println("▶️ 主程序已启动，消费者正在运行...");
        System.out.println("   (程序将在5秒后模拟发送关闭信号)");

        try {
            // 2. 让消费者线程运行5秒钟
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            // 主线程在休眠中被中断，这在简单应用中不常见，但也应该处理
            Thread.currentThread().interrupt();
        }

        // 3. ======================= 发起优雅关闭 =======================
        System.out.println("🔴 主程序发送关闭信号 (interrupt) 给消费者线程...");
        consumerThread.interrupt(); // 这就是发送中断信号的地方！

        try {
            // 4. 等待消费者线程处理完当前任务并终止 (最多等待10秒)
            // join() 方法本身也会响应中断
            consumerThread.join(TimeUnit.SECONDS.toMillis(10));
            System.out.println("👍 消费者线程已成功关闭。主程序退出。");
        } catch (InterruptedException e) {
            System.err.println("❗️ 主线程在等待消费者关闭时被中断，强制退出。");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 消息消费者，模拟从消息队列中持续获取并处理消息。
     * 这是一个典型的、需要支持优雅关闭的后台任务。
     * 定义为静态内部类，因为它不依赖于外部类的实例。
     */
    public static class MessageConsumer implements Runnable {

        // 使用阻塞队列模拟消息队列
        private final BlockingQueue<String> messageQueue;
        // 模拟一个数据库连接或其他需要被关闭的资源
        private boolean isDbConnected = false;

        public MessageConsumer() {
            // 在实际项目中，这里可能是 KafkaConsumer, RocketMQConsumer 等
            this.messageQueue = new LinkedBlockingQueue<>();
            // 模拟初始放入一些消息
            for (int i = 1; i <= 100; i++) {
                messageQueue.add("Message-" + i);
            }
        }

        @Override
        public void run() {
            System.out.println("✅ 消费者线程 [" + Thread.currentThread().getName() + "] 已启动...");
            try {
                connectToDatabase(); // 建立资源连接

                // 模式一：在循环条件中检查中断状态
                // 这是处理非阻塞型耗时操作（CPU密集型循环）中断的经典模式
                while (!Thread.currentThread().isInterrupted()) {
                    String message = null;
                    try {
                        // 模式二：处理可中断的阻塞方法
                        // take() 方法会阻塞直到队列中有可用元素。如果线程在等待时被中断，
                        // 它会清除中断状态并抛出 InterruptedException。
                        message = messageQueue.take();

                        System.out.printf(">>> 正在处理消息: [%s]%n", message);
                        // 模拟业务处理耗时，这个过程也可能被中断
                        processMessage(message);

                    } catch (InterruptedException e) {
                        // 这是实现优雅关闭的关键点！
                        System.out.println("🟡 消费者在等待或处理消息时被中断，准备关闭...");

                        // 1. 如果有正在处理的消息，进行补偿操作（可选）
                        if (message != null) {
                            System.out.printf("❗️ 中断发生，将未完全处理的消息 [%s] 放回队列...%n", message);
                            // 尝试将消息放回队列，或记录到日志/死信队列
                            messageQueue.offer(message);
                        }

                        // 2. 恢复中断状态
                        // 因为 InterruptedException 会清除中断标志位，我们需要重新设置它。
                        // 这样，外层的 `while` 循环条件 (!Thread.currentThread().isInterrupted()) 才能在下一次检查时
                        // 正确地识别到中断，从而优雅地退出循环。
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                // 无论循环是因为中断还是其他原因退出，最终都要释放资源
                releaseDatabaseConnection();
                System.out.println("🛑 消费者线程 [" + Thread.currentThread().getName() + "] 已关闭。");
            }
        }

        private void processMessage(String message) throws InterruptedException {
            // 模拟每个消息处理耗时500毫秒
            // Thread.sleep() 是另一个会抛出 InterruptedException 的典型方法
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.printf("✅ 消息 [%s] 处理完成。%n", message);
        }

        private void connectToDatabase() {
            this.isDbConnected = true;
            System.out.println("🔗 数据库连接已建立。");
        }

        private void releaseDatabaseConnection() {
            this.isDbConnected = false;
            System.out.println("🔗 数据库连接已释放。");
        }
    }
}