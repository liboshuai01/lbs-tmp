package com.liboshuai.flink;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MiniFlink {

    @FunctionalInterface
    interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
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

    interface MailboxDefaultAction {

        /**
         * 执行默认动作。
         *
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
         *
         * @param command     业务逻辑
         * @param description 调试描述
         */
        void execute(ThrowingRunnable<? extends Exception> command, String description);
    }

    /**
     * 封装了具体的任务（Runnable）和优先级。
     * 修改点：实现了 Comparable 接口，并增加了 seqNum 以保证同优先级的 FIFO 顺序。
     */
    static class Mail implements Comparable<Mail> {

        // 全局递增序列号，用于保证相同优先级邮件的提交顺序 (FIFO)
        private static final AtomicLong SEQ_COUNTER = new AtomicLong();

        // 真正的业务逻辑 (例如：执行 Checkpoint，或者处理一条数据)
        // 注意：这里假设 ThrowingRunnable 定义在包级别或 MiniFlink 中
        private final ThrowingRunnable<? extends Exception> runnable;

        // 优先级 (数字越小优先级越高)
        @Getter
        private final int priority;

        // 描述信息，用于调试 (例如 "Checkpoint 15")
        private final String description;

        // 序列号：在创建时生成，用于解决 PriorityQueue 不稳定排序的问题
        private final long seqNum;

        public Mail(ThrowingRunnable<? extends Exception> runnable, int priority, String description) {
            this.runnable = runnable;
            this.priority = priority;
            this.description = description;
            // 获取当前唯一递增序号
            this.seqNum = SEQ_COUNTER.getAndIncrement();
        }

        /**
         * 执行邮件中的逻辑
         */
        public void run() throws Exception {
            runnable.run();
        }

        @Override
        public String toString() {
            return description + " (priority=" + priority + ", seq=" + seqNum + ")";
        }

        /**
         * 优先级比较核心逻辑 (仿照 Flink 1.18)
         * 1. 优先比较 priority (数值越小，优先级越高)
         * 2. 如果 priority 相同，比较 seqNum (数值越小，提交越早，越先执行)
         */
        @Override
        public int compareTo(Mail other) {
            int priorityCompare = Integer.compare(this.priority, other.priority);
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // 优先级相同，严格按照 FIFO
            return Long.compare(this.seqNum, other.seqNum);
        }
    }

    /**
     * 邮箱的实现类。
     * 核心修改：在 take 和 tryTake 中增加了对队头元素优先级的检查。
     * 只有当 队头邮件优先级 <= 请求优先级 (priority) 时，才允许取出。
     */
    @Slf4j
    static class TaskMailboxImpl implements TaskMailbox {

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();

        // 使用 PriorityQueue 保证物理上的顺序：优先级数值小的在队头
        private final PriorityQueue<Mail> queue = new PriorityQueue<>();

        private final Thread mailboxThread;
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

        /**
         * 非阻塞获取邮件。
         * 关键逻辑：如果队头邮件的优先级比请求的 priority 低（数值大），则视为"无符合条件的邮件"，返回 Empty。
         */
        @Override
        public Optional<Mail> tryTake(int priority) {
            checkIsMailboxThread();
            lock.lock();
            try {
                Mail head = queue.peek();

                // 1. 物理队列为空
                if (head == null) {
                    return Optional.empty();
                }

                // 2. [关键模仿 Flink] 优先级不满足
                // 如果 head.priority (比如 1) > required priority (比如 0)
                // 说明虽然有信，但这封信不够格，不能在这里被取出。
                if (head.getPriority() > priority) {
                    return Optional.empty();
                }

                // 3. 满足条件，取出
                return Optional.ofNullable(queue.poll());
            } finally {
                lock.unlock();
            }
        }

        /**
         * 阻塞获取邮件。
         * 关键逻辑：只要队列为空，或者 队头邮件优先级不满足要求，就一直阻塞等待。
         */
        @Override
        public Mail take(int priority) throws InterruptedException {
            checkIsMailboxThread();
            lock.lock();
            try {
                // 循环等待条件：(队列为空) OR (有信，但信的优先级比我要求的低)
                while (isQueueEmptyOrPriorityTooLow(priority)) {
                    if (state == State.CLOSED) {
                        throw new IllegalStateException("邮箱已关闭");
                    }
                    // 阻塞等待 put() 唤醒。
                    // 注意：当新邮件放入时，put() 会 signal，此时我们会醒来重新检查 peek()
                    notEmpty.await();
                }
                // 走到这里，说明 head != null 且 head.priority <= priority
                return queue.poll();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 辅助判断逻辑：是否需要阻塞
         */
        private boolean isQueueEmptyOrPriorityTooLow(int priority) {
            Mail head = queue.peek();
            if (head == null) {
                return true; // 空，需要等
            }
            // 非空，但 head.priority (例如 1-Data) > priority (例如 0-Checkpoint)
            // 说明当前只有低优先级的信，但我想要高优先级的，所以也要等。
            return head.getPriority() > priority;
        }

        @Override
        public void put(Mail mail) {
            lock.lock();
            try {
                if (state == State.CLOSED) {
                    log.warn("邮箱已关闭，正在丢弃邮件：" + mail);
                    return;
                }
                queue.offer(mail);
                // 唤醒可能正在阻塞的 take()
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
                notEmpty.signalAll();
                queue.clear();
            } finally {
                lock.unlock();
            }
        }

        private void checkIsMailboxThread() {
            if (Thread.currentThread() != mailboxThread) {
                throw new IllegalStateException(
                        "非法线程访问。预期: " + mailboxThread.getName() +
                                ", 实际: " + Thread.currentThread().getName());
            }
        }
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

    }

    /**
     * 邮箱处理器。
     * 贴近 Flink 1.18：Controller 不再负责让步逻辑，只负责挂起。
     * 让步逻辑交由 Action 自己通过 checkMailbox 实现。
     */
    static class MailboxProcessor implements MailboxDefaultAction.Controller {

        public static final int MIN_PRIORITY = 0;
        public static final int DEFAULT_PRIORITY = 1;

        private final MailboxDefaultAction defaultAction;
        private final TaskMailbox mailbox;

        @Getter
        private final MiniFlink.MailboxExecutor mainExecutor;

        private boolean isDefaultActionAvailable = true;

        public MailboxProcessor(MailboxDefaultAction defaultAction, TaskMailbox mailbox) {
            this.defaultAction = defaultAction;
            this.mailbox = mailbox;
            this.mainExecutor = new MiniFlink.MailboxExecutorImpl(mailbox, DEFAULT_PRIORITY);
        }

        public void runMailboxLoop() throws Exception {
            while (true) {
                // 阶段 1: 处理所有待处理的邮件 (Checkpoint, Timers 等)
                // 只要有邮件，就一直处理，直到邮箱为空或只剩下优先级不够的邮件
                while (processMail(mailbox, MIN_PRIORITY)) {
                    // loop
                }

                // 阶段 2: 执行默认动作 (数据处理)
                if (isDefaultActionAvailable) {
                    // StreamInputProcessor 在内部进行批处理时，会主动检查 mailbox.hasMail()
                    defaultAction.runDefaultAction(this);
                } else {
                    // 阶段 3: 挂起，阻塞等待新邮件
                    MiniFlink.Mail mail = mailbox.take(DEFAULT_PRIORITY);
                    mail.run();
                }
            }
        }

        private boolean processMail(TaskMailbox mailbox, int priority) throws Exception {
            Optional<MiniFlink.Mail> mail = mailbox.tryTake(priority);
            if (mail.isPresent()) {
                mail.get().run();
                return true;
            }
            return false;
        }

        // --- Controller 接口实现 ---

        @Override
        public void suspendDefaultAction() {
            this.isDefaultActionAvailable = false;
        }

        public void resumeDefaultAction() {
            mailbox.put(new MiniFlink.Mail(
                    () -> this.isDefaultActionAvailable = true,
                    MIN_PRIORITY,
                    "Resume Default Action"
            ));
        }
    }

    /**
     * 任务基类。
     * 核心修改：明确区分 Control Flow (优先级0) 和 Data Flow (优先级1) 的 Executor 配置。
     */
    @Slf4j
    static abstract class StreamTask implements MailboxDefaultAction {

        protected final TaskMailbox mailbox;
        protected final MailboxProcessor mailboxProcessor;
        protected final MailboxExecutor mainMailboxExecutor;

        public StreamTask() {
            Thread currentThread = Thread.currentThread();
            this.mailbox = new TaskMailboxImpl(currentThread);
            this.mailboxProcessor = new MailboxProcessor(this, mailbox);
            // 主执行器 (用于 task 内部自提交) 跟随 Processor 的默认优先级 (1)
            this.mainMailboxExecutor = mailboxProcessor.getMainExecutor();
        }

        public final void invoke() throws Exception {
            log.info("[StreamTask] 任务已启动。");
            try {
                mailboxProcessor.runMailboxLoop();
            } catch (Exception e) {
                log.error("[StreamTask] 异常：" + e.getMessage());
                throw e;
            } finally {
                close();
            }
        }

        private void close() {
            log.info("[StreamTask] 结束。");
            mailbox.close();
        }

        /**
         * 获取控制平面执行器。
         * [关键] 强制使用 MailboxProcessor.MIN_PRIORITY (0)。
         * 只有这样，CheckpointScheduler 提交的任务才会拥有 priority=0，
         * 从而在 MailboxProcessor 的 "阶段 1" 中被优先抢占执行。
         */
        public MailboxExecutor getControlMailboxExecutor() {
            return new MailboxExecutorImpl(mailbox, MailboxProcessor.MIN_PRIORITY);
        }

        @Override
        public abstract void runDefaultAction(Controller controller) throws Exception;

        public abstract void performCheckpoint(long checkpointId);
    }

    /**
     * 对应 Flink 源码中的 InputGate。
     * 核心变化：实现了 AvailabilityProvider 模式，使用 CompletableFuture 通知数据到达。
     */
    @Slf4j
    static class MiniInputGate {

        private final Queue<String> queue = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        // 这一步是 Flink 高效的关键：如果当前没数据，就给调用者一个 Future
        // 当有数据写入时，我们 complete 这个 Future，唤醒主线程
        private CompletableFuture<Void> availabilityFuture = new CompletableFuture<>();

        /**
         * [Netty 线程调用] 写入数据
         */
        public void pushData(String data) {
            lock.lock();
            try {
                queue.add(data);
                // 如果有线程（Task线程）正在等待数据（future未完成），现在由于数据来了，不仅要由未完成变为完成
                // 还需要重置一个新的 Future 给下一次等待用？
                // 在 Flink 中，一旦 future 完成，说明"现在有数据了"。
                if (!availabilityFuture.isDone()) {
                    availabilityFuture.complete(null);
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * [Task 线程调用] 尝试获取数据
         *
         * @return 数据，如果为空则返回 null
         */
        public String pollNext() {
            lock.lock();
            try {
                String data = queue.poll();
                if (queue.isEmpty()) {
                    // 如果队列空了，重置 future，表示"目前不可用"
                    // 下次 Netty 写入时会再次 complete 它
                    if (availabilityFuture.isDone()) {
                        availabilityFuture = new CompletableFuture<>();
                    }
                }
                return data;
            } finally {
                lock.unlock();
            }
        }

        /**
         * [Task 线程调用] 获取可用性 Future
         * 只有当 InputGate 为空时，Task 才会关心这个 Future
         */
        public CompletableFuture<Void> getAvailableFuture() {
            lock.lock();
            try {
                return availabilityFuture;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 对应 Flink 源码中的 StreamOneInputProcessor。
     * 它是 MailboxDefaultAction 的具体实现者。
     * 它的职责是：不断拉取数据，如果没数据了，就告诉 Mailbox "我没活了，挂起我"。
     */
    @Slf4j
    static class StreamInputProcessor implements MailboxDefaultAction {

        private final MiniInputGate inputGate;
        private final DataOutput output;

        public interface DataOutput {
            void processRecord(String record);
        }

        public StreamInputProcessor(MiniInputGate inputGate, DataOutput output) {
            this.inputGate = inputGate;
            this.output = output;
        }

        @Override
        public void runDefaultAction(Controller controller) {
            // 1. 尝试从 InputGate 拿数据
            String record = inputGate.pollNext();

            if (record != null) {
                // A. 有数据，直接处理
                // 注意：这里是在主线程执行，非常安全
                output.processRecord(record);
            } else {
                // B. 没数据了 (InputGate 空)
//            log.info("[Task] 输入数据为空. 暂停数据处理...");

                // 1. 获取 InputGate 的"可用性凭证" (Future)
                CompletableFuture<Void> availableFuture = inputGate.getAvailableFuture();

                if (availableFuture.isDone()) {
                    // 极低概率：刚 poll 完是空，但这微秒间 Netty 又塞了一个并在 pollNext 内部 complete 了 future
                    // 那么直接 return，下一轮循环再 poll 即可
                    return;
                }

                // 2. 告诉 MailboxProcessor：暂停默认动作 (Suspend)
                // 此时主线程会停止疯狂空转，进入 mailbox.take() 的阻塞睡眠状态，或者处理其他 Mail
                controller.suspendDefaultAction();

                // 3. 核心桥梁：当 Future 完成时（即 Netty 推数据了），向 Mailbox 发送一个"恢复信号"
                // thenRun 是在 complete 这个 Future 的线程（即 Netty 线程）中执行的
                availableFuture.thenRun(() -> {
                    // 注意：这里是在 Netty 线程运行，所以要跨线程调用 resume
                    // 这会往 Mailbox 塞一个高优先级的 "Resume Mail"
//                log.debug("[Netty->Task] 数据到达，触发 Resume");
                    ((MailboxProcessor) controller).resumeDefaultAction();
                });
            }
        }
    }

    /**
     * 这是一个具体的业务 Task。
     * 目标：演示在不加锁的情况下，处理数据和 Checkpoint 读取状态的安全性。
     */
    @Slf4j
    static class CounterStreamTask extends StreamTask implements StreamInputProcessor.DataOutput {

        private final StreamInputProcessor inputProcessor;

        // === 核心状态 ===
        // 在多线程模型中，这里必须 volatile 甚至 AtomicLong，或者加 synchronized
        // 但在 Mailbox 模型中，它只是一个普通的 long，因为只有主线程能访问它！
        private long recordCount = 0;

        public CounterStreamTask(MiniInputGate inputGate) {
            super();
            // 初始化 Processor，将自己作为 Output 传入
            this.inputProcessor = new StreamInputProcessor(inputGate, this);
        }

        @Override
        public void runDefaultAction(Controller controller) {
            // 委托给 Processor 处理输入
            inputProcessor.runDefaultAction(controller);
        }

        @Override
        public void processRecord(String record) {
            // [主线程] 正在处理数据
            this.recordCount++;

            // 模拟一点计算耗时
            if (recordCount % 10 == 0) {
                log.info("Task 处理进度: {} 条", recordCount);
            }
        }

        /**
         * 执行 Checkpoint 的逻辑。
         * 这个方法会被封装成一个 Mail，由 CheckpointCoordinator 扔进邮箱。
         * 因为是从邮箱取出来执行的，所以它一定是在 [主线程] 运行。
         */
        public void performCheckpoint(long checkpointId) {
            // [主线程] 正在执行 Checkpoint
            // 此时 processRecord 绝对不会运行，因为是串行的！

            log.info(" >>> [Checkpoint Starting] ID: {}, 当前状态值: {}", checkpointId, recordCount);

            // 模拟状态快照耗时
            try { Thread.sleep(50); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.info(" <<< [Checkpoint Finished] ID: {} 完成", checkpointId);
        }
    }

    /**
     * 模拟 Netty 网络层。
     * 只负责疯狂往 InputGate 塞数据。
     */
    @Slf4j
    static class NettyDataProducer extends Thread {
        private final MiniInputGate inputGate;
        private volatile boolean running = true;

        public NettyDataProducer(MiniInputGate inputGate) {
            super("Netty-Thread");
            this.inputGate = inputGate;
        }

        @Override
        public void run() {
            Random random = new Random();
            int seq = 0;
            while (running) {
                try {
                    // 模拟网络抖动：大部分时候很快，偶尔卡顿
                    // 这能测试 Task 在"忙碌"和"挂起"状态之间的切换
                    int sleep = random.nextInt(100) < 5 ? 500 : 10;
                    TimeUnit.MILLISECONDS.sleep(sleep);

                    String data = "Record-" + (++seq);
                    inputGate.pushData(data);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void shutdown() {
            running = false;
            this.interrupt();
        }
    }

    /**
     * 模拟 JobMaster 定时触发 Checkpoint。
     * 这里的核心是：它不再自己去读取 Task 状态，而是往 Task 的邮箱里扔一个"命令"。
     */
    @Slf4j
    static class CheckpointScheduler extends Thread {

        private final MailboxExecutor taskMailboxExecutor;
        private final StreamTask task;
        private volatile boolean running = true;

        public CheckpointScheduler(CounterStreamTask task) {
            super("Checkpoint-Timer");
            this.task = task;
            // 获取高优先级的执行器 (Checkpoint 优先级 > 数据处理)
            this.taskMailboxExecutor = task.getControlMailboxExecutor();
        }

        @Override
        public void run() {
            long checkpointId = 0;
            while (running) {
                try {
                    TimeUnit.MILLISECONDS.sleep(2000); // 每2秒触发一次
                    long id = ++checkpointId;

                    log.info("[JM] 触发 Checkpoint {}", id);

                    // === 关键点 ===
                    // 我们不在这里调用 task.performCheckpoint()，因为那会导致线程不安全。
                    // 我们创建一个 Mail (Lambda)，扔给 Task 线程自己去跑。
                    taskMailboxExecutor.execute(
                            () -> task.performCheckpoint(id),
                            "Checkpoint-" + id
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void shutdown() {
            running = false;
            this.interrupt();
        }
    }

    /**
     * 新版 MiniFlink 启动类。
     * 展示了 Mailbox 模型如何协调 IO 线程、控制线程和主计算线程。
     */
    @Slf4j
    static class EntryPoint {

        public static void main(String[] args) {
            log.info("=== Flink Mailbox 模型深度模拟启动 ===");

            // 1. 构建组件
            MiniInputGate inputGate = new MiniInputGate();

            // Task 创建时会初始化自己的 Mailbox 和 Processor
            // 注意：Task 必须在主线程（即这里的 main 线程）运行逻辑
            CounterStreamTask task = new CounterStreamTask(inputGate);

            // 2. 启动外部线程
            NettyDataProducer netty = new NettyDataProducer(inputGate);
            netty.start();

            CheckpointScheduler cpCoordinator = new CheckpointScheduler(task);
            cpCoordinator.start();

            // 3. 启动 Task 主循环 (阻塞当前 Main 线程)
            try {
                // 这行代码之后，Main 线程变成了 Task 线程
                // 它会在以下状态切换：
                // - 处理 Mail (Checkpoint)
                // - 处理 DefaultAction (消费 InputGate)
                // - Suspend (等待 InputGate 的 Future)
                task.invoke();
            } catch (Exception e) {
                log.error("Task 崩溃", e);
            } finally {
                netty.shutdown();
                cpCoordinator.shutdown();
            }
        }
    }

}
