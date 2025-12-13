package com.liboshuai.demo.mailbox;


/**
 * 模拟 JobMaster 端的 Checkpoint 协调器。
 * 定期向 Task 发送 Checkpoint 请求。
 */
public class MockCheckpointCoordinator extends Thread {

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
