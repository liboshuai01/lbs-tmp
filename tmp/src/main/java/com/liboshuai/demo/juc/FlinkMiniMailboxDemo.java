package com.liboshuai.demo.juc;

import com.liboshuai.demo.function.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class FlinkMiniMailboxDemo {

    private static final Logger log = LoggerFactory.getLogger(FlinkMiniMailboxDemo.class);

    interface Mailbox {

        void put(Runnable mail);

        Runnable tryTask();

        boolean hasMail();

    }

    private static class TaskMailboxImpl implements Mailbox {

        private final ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();
        private final Thread mainThread;

        private volatile boolean hasNewMail;

        private TaskMailboxImpl(Thread mainThread) {
            this.mainThread = mainThread;
        }

        @Override
        public void put(Runnable mail) {
            if (queue.offer(mail)) {
                hasNewMail = true;
                LockSupport.unpark(mainThread);
            }
        }

        @Override
        public Runnable tryTask() {
            Runnable mail = queue.poll();
            if (mail == null) {
                hasNewMail = false;
            }
            return mail;
        }

        @Override
        public boolean hasMail() {
            return hasNewMail;
        }
    }

    private static void processMail(Mailbox mailbox) {
        if (!mailbox.hasMail()) {
            return;
        }
        Runnable mail;
        while ((mail = mailbox.tryTask()) != null) {
            mail.run();
        }
    }

    public static void main(String[] args) {
        Mailbox mailbox = new TaskMailboxImpl(Thread.currentThread());
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CompletableFuture<Void> rpcCf = CompletableFuture.runAsync(FunctionUtils.uncheckedRunnable(
                () -> {
                    TimeUnit.SECONDS.sleep(1);
                    mailbox.put(
                            () -> log.info(">>> ⏰ 触发 Checkpoint...")
                    );
                }
        ), pool);
        for (int i = 0; i < 10; i++) {
            log.info("正在处理数据: {}", i);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
            processMail(mailbox);
        }
        rpcCf.join();
        pool.shutdown();
    }

}
