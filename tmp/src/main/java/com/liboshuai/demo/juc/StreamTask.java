package com.liboshuai.demo.juc;


import java.util.concurrent.CompletableFuture;

public abstract class StreamTask implements MailboxDefaultAction {

    private final MailboxProcessor mailboxProcessor;

    // 所有的异步操作（如 Checkpoint）都通过这个 Executor 提交回主线程
    protected final MailboxExecutorImpl mainMailboxExecutor;

    public StreamTask() {
        this.mailboxProcessor = new MailboxProcessor(this);
        this.mainMailboxExecutor = mailboxProcessor.getMainThreadExecutor();
    }

    public void invoke() throws Exception {
        System.out.println("[StreamTask] 任务初始化...");
        init();

        System.out.println("[StreamTask] 进入 Mailbox 主循环...");
        mailboxProcessor.runMailboxLoop();

        System.out.println("[StreamTask] 任务退出.");
        cleanup();
    }

    protected abstract void init() throws Exception;

    /**
     * 这就是 processInput 的模拟
     */
    protected abstract void processInput(Controller controller) throws Exception;

    protected abstract void cleanup() throws Exception;

    @Override
    public void runDefaultAction(Controller controller) throws Exception {
        processInput(controller);
    }

    // --- 模拟 Checkpoint 触发逻辑 ---

    public CompletableFuture<Boolean> triggerCheckpoint(long checkpointId) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

        // 关键点：外部线程调用此方法，但我们将逻辑 "enqueue" 到主线程执行
        mainMailboxExecutor.execute(() -> {
            try {
                System.out.println("   >>> [主线程] 执行 Checkpoint " + checkpointId + " (线程安全，无锁)");
                // 在这里访问状态是安全的，因为 processInput 此时被暂停了
                performCheckpoint(checkpointId);
                resultFuture.complete(true);
            } catch (Exception e) {
                resultFuture.completeExceptionally(e);
            }
        });

        return resultFuture;
    }

    protected void performCheckpoint(long checkpointId) {
        // 子类实现具体逻辑
    }

    public void cancel() {
        mailboxProcessor.stop();
    }
}
