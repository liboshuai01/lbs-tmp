package com.liboshuai.demo.mailbox;


/**
 * 所有任务的基类。
 * 负责组装 MailboxProcessor、Executor 并管理生命周期。
 */
public abstract class StreamTask implements MailboxDefaultAction {

    protected final TaskMailbox mailbox;
    protected final MailboxProcessor mailboxProcessor;
    protected final MailboxExecutor mainMailboxExecutor;

    private volatile boolean isRunning = true;

    public StreamTask() {
        // 1. 获取当前线程作为主线程
        Thread currentThread = Thread.currentThread();

        // 2. 初始化邮箱
        this.mailbox = new TaskMailboxImpl(currentThread);

        // 3. 初始化处理器 (将 this 作为 DefaultAction 传入)
        this.mailboxProcessor = new MailboxProcessor(this, mailbox);

        // 4. 获取主线程 Executor
        this.mainMailboxExecutor = mailboxProcessor.getMainExecutor();
    }

    /**
     * 任务执行的主入口
     */
    public final void invoke() throws Exception {
        System.out.println("[StreamTask] Task started. Entering Mailbox Loop...");
        try {
            // 启动主循环，直到任务取消或完成
            mailboxProcessor.runMailboxLoop();
        } catch (Exception e) {
            System.err.println("[StreamTask] Exception in Main Loop: " + e.getMessage());
            throw e;
        } finally {
            close();
        }
    }

    public void cancel() {
        this.isRunning = false;
        mailbox.close();
    }

    private void close() {
        System.out.println("[StreamTask] Task finished/closed.");
        mailbox.close();
    }

    /**
     * 获取用于提交 Checkpoint 等控制消息的 Executor (高优先级)
     */
    public MailboxExecutor getControlMailboxExecutor() {
        // 假设优先级 10 用于控制消息
        return new MailboxExecutorImpl(mailbox, 10);
    }

    // 子类实现具体的处理逻辑
    @Override
    public abstract void runDefaultAction(Controller controller) throws Exception;
}