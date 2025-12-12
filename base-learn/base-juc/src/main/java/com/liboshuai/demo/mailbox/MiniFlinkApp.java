package com.liboshuai.demo.mailbox;

import com.liboshuai.demo.mailbox.*;

public class MiniFlinkApp {

    public static void main(String[] args) throws Exception {
        System.out.println("[Main] Starting Mini-Flink...");

        // 1. 准备组件
        Thread mainThread = Thread.currentThread();
        TaskMailbox mailbox = new TaskMailboxImpl(mainThread);

        // 使用数组容器来解决“鸡生蛋，蛋生鸡”的引用问题，
        // 让 sourceAction 内部能访问到稍后创建的 processor
        final MailboxProcessor[] processorHolder = new MailboxProcessor[1];

        // 2. 定义 DefaultAction (模拟 Source 读取)
        MailboxDefaultAction sourceAction = new MailboxDefaultAction() {
            // 使用 volatile 保证多线程可见性
            private volatile int count = 0;

            @Override
            public void runDefaultAction(Controller controller) throws Exception {
                if (count < 3) {
                    System.out.println("[Source] Processing record " + (++count));
                    Thread.sleep(500); // 模拟处理耗时
                } else {
                    System.out.println("[Source] No more data temporarily. Suspending...");
                    // 模拟没数据了，请求挂起
                    controller.suspendDefaultAction();

                    // 启动一个外部线程模拟 2秒后数据来了
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            System.out.println("[Netty] New data arrived! Resuming...");

                            // 1. 模拟新数据到达 (重置计数)
                            count = 0;

                            // 2. 真正执行唤醒逻辑
                            // 注意：在真实 Flink 中，InputGate 会持有必要的锁或上下文来调用 resume
                            if (processorHolder[0] != null) {
                                processorHolder[0].resumeDefaultAction();
                            }
                        } catch (InterruptedException e) { }
                    }).start();
                }
            }
        };

        MailboxProcessor processor = new MailboxProcessor(sourceAction, mailbox);
        // 将 processor 引用填入容器
        processorHolder[0] = processor;

        // 3. 模拟外部线程发送控制消息 (Checkpoint)
        new Thread(() -> {
            try {
                // 循环发送 Checkpoint，为了观察长期的交互效果
                for (int i = 1; i <= 3; i++) {
                    Thread.sleep(2500);
                    System.out.println("[JobMaster] Triggering Checkpoint-" + i + "...");
                    MailboxExecutor executor = new MailboxExecutorImpl(mailbox, 10);
                    int finalI = i;
                    executor.execute(() -> {
                        System.out.println(" >>> [MainThread] Performing Checkpoint-" + finalI + " (SAFE) <<<");
                    }, "Checkpoint-" + i);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        // 5. 启动引擎 (这将阻塞主线程)
        processor.runMailboxLoop();
    }
}