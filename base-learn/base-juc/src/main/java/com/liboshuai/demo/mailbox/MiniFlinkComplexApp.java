package com.liboshuai.demo.mailbox;


import java.util.Random;

public class MiniFlinkComplexApp {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting Mini-Flink (Advanced Mode) ===");

        // 1. 准备基础设施
        MockInputGate inputGate = new MockInputGate();

        // 注意：StreamTask 必须在主线程创建，因为它会绑定 Thread.currentThread()
        SourceStreamTask sourceTask = new SourceStreamTask(inputGate);

        // 2. 启动 Netty 线程 (生产者)
        MockNettyThread nettyThread = new MockNettyThread(inputGate);
        nettyThread.start();

        // 3. 启动 Checkpoint 协调器 (控制信号发送者)
        // 传入 Task 的控制执行器 (Control Executor)
        MockCheckpointCoordinator checkpointCoordinator =
                new MockCheckpointCoordinator(sourceTask.getControlMailboxExecutor());
        checkpointCoordinator.start();

        // 4. 运行任务主循环 (这将阻塞当前主线程)
        // 在此期间，主线程会在 "处理数据"、"处理Checkpoint" 和 "挂起等待" 三种状态间切换
        try {
            sourceTask.invoke();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 清理资源
            nettyThread.shutdown();
            checkpointCoordinator.shutdown();
        }
    }

    static class MockNettyThread extends Thread {

        private final MockInputGate inputGate;
        private volatile boolean running = true;

        public MockNettyThread(MockInputGate inputGate) {
            super("Netty-IO-Thread");
            this.inputGate = inputGate;
        }

        @Override
        public void run() {
            int seq = 0;
            Random random = new Random();

            while (running) {
                try {
                    // 1. 模拟网络数据到达的不确定性 (时快时慢)
                    // 有时很快 (100ms)，有时会卡顿 (2000ms)，从而触发 Task 挂起
                    int sleepTime = random.nextInt(10) > 7 ? 2000 : 100;
                    Thread.sleep(sleepTime);

                    // 2. 生成数据
                    String data = "Record-" + (++seq);

                    // 3. 推送到 InputGate (这可能会触发 SourceTask 的 resume)
                    // System.out.println("[Netty] Receiving " + data);
                    inputGate.pushData(data);

                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void shutdown() {
            running = false;
            this.interrupt();
        }
    }

    static class MockCheckpointCoordinator extends Thread {

        private final MailboxExecutor taskExecutor;
        private volatile boolean running = true;

        public MockCheckpointCoordinator(MailboxExecutor taskExecutor) {
            super("Checkpoint-Coordinator");
            this.taskExecutor = taskExecutor;
        }

        @Override
        public void run() {
            long checkpointId = 1;
            while (running) {
                try {
                    // 每 1.5 秒触发一次 Checkpoint
                    Thread.sleep(1500);

                    long currentId = checkpointId++;
                    System.out.println("[JM] Triggering Checkpoint " + currentId);

                    // 提交给 Task 主线程执行 (线程安全！)
                    taskExecutor.execute(() -> {
                        System.out.println(" >>> [MainThread] Doing Checkpoint " + currentId + " (State Snapshot) <<<");
                        // 模拟 Checkpoint 耗时
                        try { Thread.sleep(50); } catch (Exception e) {}
                    }, "Checkpoint-" + currentId);

                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void shutdown() {
            running = false;
            this.interrupt();
        }
    }
}
