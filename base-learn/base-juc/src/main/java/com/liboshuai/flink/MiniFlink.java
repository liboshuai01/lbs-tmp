package com.liboshuai.flink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MiniFlink {

    @FunctionalInterface
    interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
    }

    static class Mail {

        // 真正的业务逻辑 (例如：执行 Checkpoint，或者处理一条数据)
        private final ThrowingRunnable<? extends Exception> runnable;

        // 优先级 (数字越小优先级越高，虽在简易版中我们暂按 FIFO 处理，但保留字段)
        @Getter
        private final int priority;

        // 描述信息，用于调试 (例如 "Checkpoint 15")
        private final String description;

        public Mail(ThrowingRunnable<? extends Exception> runnable, int priority, String description) {
            this.runnable = runnable;
            this.priority = priority;
            this.description = description;
        }

        /**
         * 执行邮件中的逻辑
         */
        public void run() throws Exception {
            runnable.run();
        }

        @Override
        public String toString() {
            return description;
        }
    }

    interface TaskMailbox {

        /**
         * 邮箱是否包含邮件
         */
        boolean hasMail();

        /**
         * 非阻塞式获取邮件 (如果没有则返回 Empty)
         */
        Optional<Mail> tryTake(int priority);

        /**
         * 阻塞式获取邮件 (如果为空则等待，直到有邮件或邮箱关闭)
         * 必须由主线程调用
         */
        Mail take(int priority) throws InterruptedException;

        /**
         * 放入邮件 (任何线程都可调用)
         */
        void put(Mail mail);

        /**
         * 关闭邮箱，不再接受新邮件，并唤醒所有等待线程
         */
        void close();

        /**
         * 邮箱状态
         */
        enum State {
            OPEN,
            QUIESCED, // 暂停处理
            CLOSED    // 彻底关闭
        }
    }

    @Slf4j
    static class TaskMailboxImpl implements TaskMailbox {

        // 核心锁
        private final ReentrantLock lock = new ReentrantLock();

        // 条件变量：队列不为空
        private final Condition notEmpty = lock.newCondition();

        // 内部队列，使用非线程安全的 ArrayDeque 即可，因为我们有 lock 保护
        private final Deque<Mail> queue = new ArrayDeque<>();

        // 邮箱所属的主线程
        private final Thread mailboxThread;

        // 邮箱状态
        private volatile State state = State.OPEN;

        public TaskMailboxImpl(Thread mailboxThread) {
            this.mailboxThread = mailboxThread;
        }

        @Override
        public boolean hasMail() {
            lock.lock();
            try {
                return !queue.isEmpty();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Optional<Mail> tryTake(int priority) {
            checkIsMailboxThread(); // 只有主线程能取信
            lock.lock();
            try {
                return Optional.ofNullable(queue.pollFirst());
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Mail take(int priority) throws InterruptedException {
            checkIsMailboxThread(); // 只有主线程能取信
            lock.lock();
            try {
                // 循环等待模式 (Standard Monitor Pattern)
                while (queue.isEmpty()) {
                    if (state == State.CLOSED) {
                        throw new IllegalStateException("邮箱已关闭");
                    }
                    // 阻塞，释放锁，等待被 put() 唤醒
                    notEmpty.await();
                }
                return queue.pollFirst();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void put(Mail mail) {
            lock.lock();
            try {
                if (state == State.CLOSED) {
                    // 如果关闭了，静默丢弃或抛异常，这里我们打印日志
                    log.warn("邮箱已关闭，正在丢弃邮件：" + mail);
                    return;
                }
                // 入队
                queue.addLast(mail);
                // 唤醒睡在 take() 里的主线程
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() {
            lock.lock();
            try {
                state = State.CLOSED;
                // 唤醒所有等待的线程，让它们抛出异常或退出
                notEmpty.signalAll();
                // 清空剩余邮件 (在真实 Flink 中通常会把剩余的执行完或做清理)
                queue.clear();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 关键防御性编程：确保单线程模型不被破坏
         */
        private void checkIsMailboxThread() {
            if (Thread.currentThread() != mailboxThread) {
                throw new IllegalStateException(
                        "非法线程访问。" +
                                "预期: " + mailboxThread.getName() +
                                ", 实际: " + Thread.currentThread().getName());
            }
        }
    }

    interface MailboxDefaultAction {

        /**
         * 执行默认动作。
         * @param controller 用于与主循环交互（例如请求挂起）
         */
        void runDefaultAction(Controller controller) throws Exception;

        /**
         * 控制器：允许 DefaultAction 影响主循环的行为
         */
        interface Controller {
            /**
             * 告诉主循环：“我没活干了（Input 为空），请暂停调度我。”
             * 调用此方法后，主循环将不再调用 runDefaultAction，直到被 resume。
             */
            void suspendDefaultAction();
        }
    }

    interface MailboxExecutor {

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

    static class MailboxExecutorImpl implements MailboxExecutor {

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

    static class MailboxProcessor implements MailboxDefaultAction.Controller {

        private final MailboxDefaultAction defaultAction;
        private final TaskMailbox mailbox;
        @Getter
        private final MailboxExecutor mainExecutor;

        // 标记默认动作是否可用（是否可以执行 processInput）
        private boolean isDefaultActionAvailable = true;

        public MailboxProcessor(MailboxDefaultAction defaultAction, TaskMailbox mailbox) {
            this.defaultAction = defaultAction;
            this.mailbox = mailbox;
            // 创建一个绑定了最低优先级的 Executor 给主线程自己用
            this.mainExecutor = new MailboxExecutorImpl(mailbox, 0);
        }

        /**
         * 启动主循环 (The Main Loop)
         */
        public void runMailboxLoop() throws Exception {

            while (true) {
                // 阶段 1: 处理所有积压的邮件 (系统事件优先)
                // 我们使用 tryTake 非阻塞地把邮箱清空
                while (mailbox.hasMail()) {
                    Optional<Mail> mail = mailbox.tryTake(0);
                    if (mail.isPresent()) {
                        mail.get().run();
                    }
                }

                // 阶段 2: 执行默认动作 (数据处理)
                if (isDefaultActionAvailable) {
                    // 执行一小步数据处理
                    defaultAction.runDefaultAction(this);
                } else {
                    // 阶段 3: 如果没事干 (DefaultAction 被挂起)，就阻塞等待新邮件
                    // take() 会让线程睡着，直到有外部线程 put()
                    Mail mail = mailbox.take(0);
                    mail.run();
                }
            }
        }

        // --- Controller 接口实现 ---

        @Override
        public void suspendDefaultAction() {
            // 收到暂停请求
            this.isDefaultActionAvailable = false;
        }

        /**
         * 这是一个用于恢复默认动作的辅助方法。
         * 外部线程（如 Netty）调用此方法来“唤醒”主循环。
         */
        public void resumeDefaultAction() {
            // 我们通过发一个特殊的“空邮件”或者“恢复邮件”来唤醒阻塞在 take() 的主线程
            // 同时修改标记位
            mainExecutor.execute(() -> {
                this.isDefaultActionAvailable = true;
            }, "Resume Default Action");
        }
    }

    @Slf4j
    static abstract class StreamTask implements MailboxDefaultAction {

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
            log.info("[StreamTask] 任务已启动。正在进入邮箱循环...");
            try {
                // 启动主循环，直到任务取消或完成
                mailboxProcessor.runMailboxLoop();
            } catch (Exception e) {
                log.error("[StreamTask] 主循环异常：" + e.getMessage());
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
            log.info("[StreamTask] 任务已完成/结束。");
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

    static class MockInputGate {

        private final Queue<String> queue = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        // 当有新数据到达时，需要执行的回调（通常是恢复 Task 的执行）
        private Runnable availabilityListener;

        /**
         * [Netty 线程调用] 模拟从网络收到数据
         */
        public void pushData(String data) {
            Runnable listener = null;
            lock.lock();
            try {
                queue.add(data);
                // 如果 Task 因为没数据挂起了（注册了监听器），现在数据来了，通过监听器唤醒它
                if (availabilityListener != null) {
                    listener = this.availabilityListener;
                    this.availabilityListener = null; // 触发一次后移除，避免重复触发
                }
            } finally {
                lock.unlock();
            }
            // 放到锁外面, 防止出现死锁等未知问题
            if (listener != null) {
                listener.run();
            }
        }

        /**
         * [Task 线程调用] 获取数据
         * @return 数据，或者 null (如果没有数据)
         */
        public String poll() {
            lock.lock();
            try {
                return queue.poll();
            } finally {
                lock.unlock();
            }
        }

        /**
         * [Task 线程调用] 注册一个监听器，当有数据到达时调用
         */
        public void registerAvailabilityListener(Runnable listener) {
            lock.lock();
            try {
                if (!queue.isEmpty()) {
                    // 如果恰好刚有数据进来，直接执行，不需要等待
                    listener.run();
                } else {
                    this.availabilityListener = listener;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Slf4j
    static class SourceStreamTask extends StreamTask {

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
                log.info("   [Task] 输入为空。暂停默认操作...");

                // 这一步告诉 MailboxProcessor：别调我了，我要睡了
                controller.suspendDefaultAction();

                // 关键：注册一个回调给 InputGate。
                // 当 Netty 线程往 InputGate 塞数据时，会调用这个回调。
                inputGate.registerAvailabilityListener(() -> {
                    // 回调逻辑：在其他线程中被调用，用来唤醒主线程
                    log.info("   [Netty->Task] 检测到新数据！正在唤醒任务...");

                    // 通过 MailboxProcessor 的 resume 机制（发信或修改标记）来恢复
                    mailboxProcessor.resumeDefaultAction();
                });
            }
        }

        private void processRecord(String record) {
            // 模拟业务处理
            log.info("   [Task] 处理中: " + record);
            try {
                TimeUnit.MILLISECONDS.sleep(200); // 模拟计算耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }



    static class MockNettyThread extends Thread {

        private final MockInputGate inputGate;
        private volatile boolean running = true;

        public MockNettyThread(MockInputGate inputGate) {
            super("Netty-IO-Thread");
            this.inputGate = inputGate;
        }

        @Override
        public void run() {
            int seq = 0;
            Random random = new Random();

            while (running) {
                try {
                    // 1. 模拟网络数据到达的不确定性 (时快时慢)
                    // 有时很快 (100ms)，有时会卡顿 (2000ms)，从而触发 Task 挂起
                    int sleepTime = random.nextInt(10) > 7 ? 2000 : 100;
                    TimeUnit.MILLISECONDS.sleep(sleepTime);

                    // 2. 生成数据
                    String data = "Record-" + (++seq);

                    // 3. 推送到 InputGate (这可能会触发 SourceTask 的 resume)
                    // log.info("[Netty] 接收中 " + data);
                    inputGate.pushData(data);

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

    @Slf4j
    static class MockCheckpointCoordinator extends Thread {

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
                    TimeUnit.MILLISECONDS.sleep(1500);

                    long currentId = checkpointId++;
                    log.info("[JM] 触发检查点 " + currentId);

                    // 提交给 Task 主线程执行 (线程安全！)
                    taskExecutor.execute(() -> {
                        log.info(" >>> [MainThread] 正在执行检查点 " + currentId + " (状态快照) <<<");
                        // 模拟 Checkpoint 耗时
                        try { TimeUnit.MILLISECONDS.sleep(50); } catch (Exception e) {}
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

    @Slf4j
    static class MiniFlinkComplexApp {

        public static void main(String[] args) throws Exception {
            log.info("=== 启动 Mini-Flink（高级模式） ===");

            // 1. 准备基础设施
            MockInputGate inputGate = new MockInputGate();

            // 注意：StreamTask 必须在主线程创建，因为它会绑定 Thread.currentThread()
            SourceStreamTask sourceTask = new SourceStreamTask(inputGate);

            // 2. 启动 Netty 线程 (生产者)
            MockNettyThread nettyThread = new MockNettyThread(inputGate);
            nettyThread.start();

            // 3. 启动 Checkpoint 协调器 (控制信号发送者)
            // 传入 Task 的控制执行器 (Control Executor)
            MockCheckpointCoordinator checkpointCoordinator =
                    new MockCheckpointCoordinator(sourceTask.getControlMailboxExecutor());
            checkpointCoordinator.start();

            // 4. 运行任务主循环 (这将阻塞当前主线程)
            // 在此期间，主线程会在 "处理数据"、"处理Checkpoint" 和 "挂起等待" 三种状态间切换
            try {
                sourceTask.invoke();
            } catch (Exception e) {
                log.error("", e);
            } finally {
                // 清理资源
                nettyThread.shutdown();
                checkpointCoordinator.shutdown();
            }
        }
    }

}
