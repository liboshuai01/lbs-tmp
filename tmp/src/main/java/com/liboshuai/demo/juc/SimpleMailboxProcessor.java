package com.liboshuai.demo.juc;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleMailboxProcessor {

    // 邮箱队列：存放待执行的动作 (Runnable)
    // 思考：为什么这里要用 ConcurrentLinkedQueue 而不是普通的 LinkedList？
    private final Queue<Runnable> mailbox = new ConcurrentLinkedQueue<>();

    // 控制处理器的运行状态
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    // 专门负责处理消息的主线程
    private final Thread processorThread;

    public SimpleMailboxProcessor() {
        this.processorThread = new Thread(this::runMailboxLoop, "Mailbox-Thread");
    }

    public void start() {
        processorThread.start();
    }

    public void stop() {
        isRunning.set(false);
    }

    // 外部线程调用：投递消息
    public void submit(Runnable action) {
        // TODO: 将任务放入邮箱
    }

    // 内部主线程循环：处理消息
    private void runMailboxLoop() {
        System.out.println("Mailbox Loop started...");
        while (isRunning.get()) {
            // TODO: 1. 从邮箱取出一封信
            // Runnable mail = ???

            // TODO: 2. 如果有信，执行它 (run)
            // TODO: 3. 如果没信 (mail == null)，该怎么办？(避免死循环空转烧 CPU)
        }
        System.out.println("Mailbox Loop finished.");
    }

    // --- 测试程序 ---
    public static void main(String[] args) throws InterruptedException {
        SimpleMailboxProcessor processor = new SimpleMailboxProcessor();
        processor.start();

        // 模拟多个外部线程并发投递任务
        for (int i = 0; i < 5; i++) {
            final int id = i;
            new Thread(() -> {
                processor.submit(() -> {
                    // 注意：这里看起来像是并发提交，但执行时是串行的！
                    // 所以操作非线程安全的资源也是安全的。
                    System.out.println(Thread.currentThread().getName() + " executing task-" + id);
                });
            }, "Producer-" + i).start();
        }

        Thread.sleep(1000);
        processor.stop();
    }
}