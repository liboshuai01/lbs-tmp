package com.liboshuai.demo.mailbox;


public class MailboxExecutorImpl implements MailboxExecutor {

    private final TaskMailbox mailbox;
    private final int priority;

    public MailboxExecutorImpl(TaskMailbox mailbox, int priority) {
        this.mailbox = mailbox;
        this.priority = priority;
    }

    @Override
    public void execute(ThrowingRunnable<? extends Exception> command, String description) {
        // 包装成 Mail 并扔进邮箱
        mailbox.put(new Mail(command, priority, description));
    }

    @Override
    public void yield() throws Exception {
        // 尝试去邮箱取信（利用当前 Executor 的优先级）
        // 注意：简单起见，这里演示的是阻塞式 yield，如果邮箱空了会等待
        // 真实 Flink 中通常使用 tryYield (非阻塞)
        Mail mail = mailbox.take(priority);
        mail.run();
    }
}
