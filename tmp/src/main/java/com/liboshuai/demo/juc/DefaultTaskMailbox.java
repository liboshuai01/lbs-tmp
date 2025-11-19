package com.liboshuai.demo.juc;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultTaskMailbox implements TaskMailbox {

    // 也就是 Flink 源码中的 "Mail"，本质就是 Runnable
    private final BlockingQueue<Runnable> queue;
    private volatile boolean open;

    // 毒丸对象，用于优雅停止循环
    public static final Runnable POISON_LETTER = () -> {};

    public DefaultTaskMailbox() {
        this.queue = new LinkedBlockingQueue<>();
        this.open = true;
    }

    @Override
    public void put(Runnable mail) throws InterruptedException {
        if (!open) {
            throw new IllegalStateException("Mailbox is closed");
        }
        queue.put(mail);
    }

    @Override
    public Runnable take() throws InterruptedException {
        // 阻塞等待新消息
        return queue.take();
    }

    @Override
    public void close() {
        this.open = false;
        // 发送毒丸唤醒可能阻塞的消费者
        queue.offer(POISON_LETTER);
    }
}
