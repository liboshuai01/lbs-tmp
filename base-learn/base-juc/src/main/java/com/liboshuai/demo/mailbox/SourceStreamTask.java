package com.liboshuai.demo.mailbox;


/**
 * 一个具体的 Source 任务实现。
 * 模拟：从 InputGate 读取数据 -> 处理 -> 没数据时挂起。
 */
public class SourceStreamTask extends StreamTask {

    private final MockInputGate inputGate;

    public SourceStreamTask(MockInputGate inputGate) {
        super();
        this.inputGate = inputGate;
    }

    @Override
    public void runDefaultAction(Controller controller) throws Exception {
        // 1. 尝试读取数据
        String record = inputGate.poll();

        if (record != null) {
            // A. 有数据：处理数据
            processRecord(record);
        } else {
            // B. 没数据：请求挂起 (Suspend)
            System.out.println("   [Task] Input empty. Suspending default action...");

            // 这一步告诉 MailboxProcessor：别调我了，我要睡了
            controller.suspendDefaultAction();

            // 关键：注册一个回调给 InputGate。
            // 当 Netty 线程往 InputGate 塞数据时，会调用这个回调。
            inputGate.registerAvailabilityListener(() -> {
                // 回调逻辑：在其他线程中被调用，用来唤醒主线程
                System.out.println("   [Netty->Task] New data detected! Waking up task...");

                // 通过 MailboxProcessor 的 resume 机制（发信或修改标记）来恢复
                mailboxProcessor.resumeDefaultAction();
            });
        }
    }

    private void processRecord(String record) {
        // 模拟业务处理
        System.out.println("   [Task] Processing: " + record);
        try {
            Thread.sleep(200); // 模拟计算耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}