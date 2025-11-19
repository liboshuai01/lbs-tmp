package com.liboshuai.demo.juc;


import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class MailboxExecutorImpl implements Executor {

    private final TaskMailbox mailbox;
    private final int priority;

    public MailboxExecutorImpl(TaskMailbox mailbox, int priority) {
        this.mailbox = mailbox;
        this.priority = priority;
    }

    @Override
    public void execute(Runnable command) {
        try {
            mailbox.put(command);
        } catch (Exception e) {
            throw new RejectedExecutionException("Mailbox is closed");
        }
    }
}
