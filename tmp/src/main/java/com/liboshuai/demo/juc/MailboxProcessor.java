package com.liboshuai.demo.juc;

public class MailboxProcessor implements Runnable {

    private final TaskMailbox mailbox;
    private final Runnable mainAction; // 模拟 Task 的主要工作（例如读取数据流）
    private volatile boolean running = true;

    public MailboxProcessor(Runnable mainAction) {
        this.mailbox = new DefaultTaskMailbox();
        this.mainAction = mainAction;
    }

    public TaskMailbox getMailbox() {
        return mailbox;
    }

    @Override
    public void run() {
        System.out.println("[MailboxThread] 启动主处理循环...");

        while (running) {
            try {
                // 1. 模拟执行一部分 Task 的常规工作 (例如从 InputGate 读一条数据)
                // 在实际 Flink 中，这里会是非阻塞的
                if (mainAction != null) {
                    mainAction.run();
                }

                // 2. 处理邮箱中的事件 (非阻塞检查或阻塞等待，视具体实现而定)
                // 为了演示清晰，我们这里假设如果没有主要工作，就阻塞等待邮箱消息
                // 实际上 Flink 会交替处理 数据流 和 邮箱事件
                processMail();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("[MailboxThread] 循环结束.");
    }

    private void processMail() throws InterruptedException {
        // 从邮箱取出动作并执行
        Runnable mail = mailbox.take();

        if (mail == DefaultTaskMailbox.POISON_LETTER) {
            running = false;
            return;
        }

        // 关键点：直接在当前主线程执行这个 Runnable
        // 此时不需要任何锁，因为是串行执行的
        mail.run();
    }

    public void stop() {
        mailbox.close();
    }
}
