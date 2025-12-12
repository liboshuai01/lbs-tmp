package com.liboshuai.demo.mailbox;


public interface MailboxExecutor {

    /**
     * 提交一个任务到邮箱。
     * @param command 业务逻辑
     * @param description 调试描述
     */
    void execute(ThrowingRunnable<? extends Exception> command, String description);

    /**
     * 让步：暂停当前正在做的事，去处理邮箱里积压的邮件。
     * 用于防止主线程被长时间占用。
     */
    void yield() throws InterruptedException, Exception;
}
