package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LbsThreadPoolExecutor 是一个自定义的、模仿 J.U.C (java.util.concurrent) 中 ThreadPoolExecutor 实现的线程池。
 * 它旨在清晰地揭示线程池的核心工作原理，包括：
 * <p>
 * 1.  <b>线程池状态管理</b>：通过一个原子整数 {@code runState} 来表示线程池的生命周期状态（RUNNING, SHUTDOWN, STOP），
 * 确保线程池在不同状态下行为正确。</br>
 * 2.  <b>工作线程管理</b>：通过一个内部类 {@code Worker} 来封装工作线程 ({@code Thread}) 和其执行的任务 ({@code Runnable})。
 * 所有工作线程都存储在 {@code HashSet<Worker>} 中进行管理。</br>
 * 3.  <b>核心执行逻辑 {@code execute()}</b>：这是任务提交的入口，它精确地定义了任务的处理流程：</br>
 * - 如果工作线程数 < corePoolSize，则创建新线程执行任务。</br>
 * - 如果工作线程数 >= corePoolSize，则尝试将任务加入工作队列。</br>
 * - 如果队列已满，则尝试创建新线程（非核心线程），直到总数达到 maximumPoolSize。</br>
 * - 如果总线程数已达上限，则执行拒绝策略。</br>
 * 4.  <b>任务获取与线程存活</b>：工作线程通过 {@code getTask()} 方法从队列中获取任务。该方法实现了核心线程的阻塞等待
 * 和非核心线程的超时等待 (keepAliveTime) 机制，从而实现了线程池的自动伸缩。</br>
 * 5.  <b>锁与并发控制</b>：使用 {@code ReentrantLock} (mainLock) 来保护对工作线程集合 {@code workers} 的并发访问，
 * 确保在添加、移除工作线程时的线程安全。而线程池状态 {@code runState} 和工作线程计数 {@code workerCount}
 * 则使用原子类来保证其操作的原子性，减少锁的粒度，提高性能。</br>
 * 6.  <b>优雅关闭与立即关闭</b>：提供了 {@code shutdown()} 和 {@code shutdownNow()} 两种关闭策略，
 * 前者会等待队列任务执行完毕，后者会尝试中断所有线程并清空队列。
 * </p>
 */
@Slf4j
public class LbsThreadPoolExecutor {

    // --- 线程池状态 ---
    /**
     * runState 是一个核心的原子变量，用于表示线程池的生命周期状态。</br>
     * 使用高3位表示状态，低29位表示工作线程数量（这是 JUC 的设计，此处简化为只存状态）。</br>
     * 状态转换是单向的：RUNNING -> SHUTDOWN -> STOP</br>
     */
    private final AtomicInteger runState = new AtomicInteger(RUNNING);

    /**
     * 状态：运行中。</br>
     * - 接受新任务。</br>
     * - 处理队列中的任务。</br>
     */
    private static final int RUNNING = 0;

    /**
     * 状态：关闭中。</br>
     * - 不再接受新任务。</br>
     * - 但会继续处理工作队列中已存在的任务。</br>
     * - 调用 shutdown() 方法会进入此状态。</br>
     */
    private static final int SHUTDOWN = 1;

    /**
     * 状态：已停止。</br>
     * - 不再接受新任务。</br>
     * - 不再处理队列中的任务。</br>
     * - 尝试中断正在执行任务的线程。</br>
     * - 调用 shutdownNow() 方法会进入此状态。</br>
     */
    private static final int STOP = 2;

    // --- 核心参数 (构造函数传入) ---
    /**
     * 核心线程数：即使空闲也保持存活的线程数量。
     */
    private final int corePoolSize;
    /**
     * 最大线程数：线程池允许创建的最大线程数量。
     */
    private final int maximumPoolSize;
    /**
     * 线程存活时间：当线程数超过核心线程数时，多余的空闲线程在被销毁前等待新任务的最长时间。
     */
    private final long keepAliveTime;
    /**
     * 存活时间的单位。
     */
    private final TimeUnit unit;
    /**
     * 工作队列：用于缓存在核心线程都在忙时提交的任务。
     */
    private final BlockingQueue<Runnable> workQueue;
    /**
     * 拒绝策略处理器：当线程池和队列都满时，用于处理新提交任务的策略。
     */
    private final LbsRejectedExecutionHandler handler;
    /**
     * 线程工厂：用于创建新的工作线程，可以自定义线程名称、是否为守护线程等。
     */
    private final ThreadFactory threadFactory;

    // --- 状态与工作集 ---
    /**
     * workerCount：当前存活的工作线程数量的原子计数器。</br>
     * 使用 AtomicInteger 是为了在不加锁的情况下安全地增减线程数。</br>
     */
    private final AtomicInteger workerCount = new AtomicInteger(0);

    /**
     * workers：存储所有工作线程的集合。</br>
     * 使用 HashSet 是为了方便地添加和移除 Worker 对象。</br>
     * 对这个集合的所有访问（添加、移除、遍历）都必须在 mainLock 的保护下进行。</br>
     */
    private final HashSet<Worker> workers = new HashSet<>();

    /**
     * mainLock：一个可重入锁，用于保护对 workers 集合的访问。</br>
     * 它是线程池中并发控制的关键，防止在多线程环境下对 worker 集合的修改导致数据不一致。</br>
     * 例如，在 addWorker 和 processWorkerExit 方法中，对 workers 的所有操作都被此锁保护。</br>
     */
    private final ReentrantLock mainLock = new ReentrantLock();


    /**
     * 全参数构造函数。
     *
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   非核心线程的空闲存活时间
     * @param unit            时间单位
     * @param workQueue       任务阻塞队列
     * @param threadFactory   线程工厂
     * @param handler         拒绝策略
     * @throws IllegalArgumentException 如果线程池参数不合法
     * @throws NullPointerException     如果队列、工厂或拒绝策略处理器为 null
     */
    public LbsThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory,
                                 LbsRejectedExecutionHandler handler) {
        // 参数合法性校验
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
            throw new IllegalArgumentException("无效的线程池参数");
        }
        if (workQueue == null || threadFactory == null || handler == null) {
            throw new NullPointerException();
        }
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 使用默认线程工厂的构造函数。
     *
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   非核心线程的空闲存活时间
     * @param unit            时间单位
     * @param workQueue       任务阻塞队列
     * @param handler         拒绝策略
     */
    public LbsThreadPoolExecutor(int corePoolSize,
                                 int maximumPoolSize,
                                 long keepAliveTime,
                                 TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 LbsRejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new DefaultThreadFactory(), handler);
    }

    /**
     * 线程池的核心公共方法：执行任务。</br>
     * 这个方法定义了任务提交后的完整处理流程，是线程池策略的集中体现。</br>
     *
     * @param command 需要执行的任务
     * @throws NullPointerException 如果任务为 null
     */
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }

        // 步骤 1: 检查线程池是否在运行。如果已关闭，则直接执行拒绝策略。
        if (runState.get() != RUNNING) {
            reject(command);
            return;
        }

        // 步骤 2: 尝试创建核心线程。
        // 如果当前工作线程数小于核心线程数，则尝试创建一个新的 Worker (核心线程) 来执行任务。
        // addWorker 方法会原子性地增加 workerCount 并启动线程。
        if (workerCount.get() < corePoolSize) {
            // addWorker 成功会直接启动线程执行任务，并返回 true。
            if (addWorker(command, true)) {
                return;
            }
        }

        // 步骤 3: 尝试将任务加入队列。
        // 如果无法创建核心线程（例如，由于并发导致其他线程已经创建满了），或者核心线程池已满，
        // 则尝试将任务放入工作队列 workQueue。
        if (workQueue.offer(command)) {
            // 双重检查 (Double-check): 任务入队后，必须再次检查线程池状态。
            // 这是一个关键的并发处理点：可能在 offer 成功之后，线程池状态变为 SHUTDOWN。
            if (runState.get() != RUNNING) {
                // 如果线程池已关闭，则需要将刚刚入队的任务移除，并执行拒绝策略，以保证关闭语义的正确性。
                if (workQueue.remove(command)) {
                    reject(command);
                }
            }
            // 【关键修复/优化】
            // 在任务成功入队后，如果当前没有任何工作线程（可能之前的线程都因为异常或 keepAliveTime 而退出了），
            // 就必须创建一个新的 Worker 来处理队列中的任务，否则队列中的任务将永远得不到执行。
            // 这个 Worker 的 firstTask 为 null，它会直接去队列里拉取任务。
            else if (workerCount.get() == 0) {
                addWorker(null, false); // 创建一个非核心线程来处理任务
            }
        } else {
            // 步骤 4: 尝试创建非核心线程。
            // 如果工作队列已满 (workQueue.offer 返回 false)，则尝试创建非核心线程来执行此任务。
            // addWorker 的第二个参数为 false，表示这次是创建非核心线程，需要检查 maximumPoolSize 限制。
            if (!addWorker(command, false)) {
                // 步骤 5: 执行拒绝策略。
                // 如果 addWorker 仍然失败（意味着当前线程数已经达到 maximumPoolSize），
                // 那么线程池已达到容量极限，只能执行拒绝策略。
                reject(command);
            }
        }
    }

    /**
     * 优雅关闭线程池。
     * <p>
     * - 将线程池状态切换到 SHUTDOWN。
     * - 之后，线程池不再接受新提交的任务。
     * - 正在执行的任务和队列中等待的任务会继续被处理。
     * - 当所有任务都执行完毕后，所有工作线程会因为从 getTask() 中获取不到任务而自然退出。
     * </p>
     */
    public void shutdown() {
        // 通过 CAS 原子操作将状态从 RUNNING 设置为 SHUTDOWN
        runState.compareAndSet(RUNNING, SHUTDOWN);
        // 注意：这里不需要手动中断线程。getTask() 方法的逻辑会检查 SHUTDOWN 状态，
        // 并在队列为空时返回 null，从而使空闲线程能够自动终止。
    }

    /**
     * 立即关闭线程池。
     * <p>
     * - 将线程池状态切换到 STOP。
     * - 尝试中断所有正在执行任务的工作线程。
     * - 从工作队列中移除所有等待的任务，并将它们作为列表返回。
     * - 不再接受任何新任务。
     * </p>
     *
     * @return 队列中尚未执行的任务列表。
     */
    public List<Runnable> shutdownNow() {
        mainLock.lock();
        try {
            // 推进状态至 STOP，确保不会再有新线程被创建或任务被处理
            if (runState.get() < STOP) {
                runState.set(STOP);
            }
            // 中断所有工作线程。这会让阻塞在 getTask() (如 workQueue.take()) 的线程立即抛出 InterruptedException。
            for (Worker w : workers) {
                try {
                    w.thread.interrupt();
                } catch (SecurityException ignore) {
                    // 忽略可能出现的安全权限问题
                }
            }
            // 排空工作队列，将未执行的任务收集起来
            List<Runnable> unexecutedTasks = new ArrayList<>();
            workQueue.drainTo(unexecutedTasks);
            return unexecutedTasks;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 判断线程池是否已经关闭（处于 SHUTDOWN 或 STOP 状态）。
     *
     * @return 如果已经关闭，返回 true。
     */
    public boolean isShutdown() {
        return runState.get() != RUNNING;
    }

    /**
     * 获取线程池使用的工作队列。
     *
     * @return 阻塞队列实例。
     */
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * 内部方法，用于执行拒绝策略。
     *
     * @param command 被拒绝的任务
     */
    private void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 添加工作线程的核心逻辑，这是线程池中并发控制最复杂的部分之一。
     *
     * @param firstTask 新线程的第一个任务，可以为 null。如果为 null，线程启动后会直接去队列取任务。
     * @param core      标记此次添加的是否为核心线程。true 表示核心线程，受 corePoolSize 限制；
     *                  false 表示非核心线程，受 maximumPoolSize 限制。
     * @return 如果成功添加并启动了线程，则返回 true，否则返回 false。
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        mainLock.lock();
        try {
            int rs = runState.get();
            // 检查线程池状态是否允许添加新线程。
            // - 如果状态 >= SHUTDOWN，则不能添加。
            // - 特殊情况：如果状态是 SHUTDOWN，但 firstTask 为 null 且队列不为空，则允许添加一个 "无任务" 的线程
            //   来帮助清理队列。这是 JUC 中的一个设计，此处简化了逻辑：只要是 SHUTDOWN 或更高状态，
            //   并且有新任务要提交 (firstTask != null)，就直接拒绝。
            if (rs >= SHUTDOWN && (rs >= STOP || firstTask != null || workQueue.isEmpty())) {
                return false;
            }

            int wc = workerCount.get();
            int poolSize = core ? corePoolSize : maximumPoolSize;
            // 检查是否已达到线程数限制
            if (wc >= poolSize) {
                return false;
            }

            // 原子性地增加工作线程计数
            workerCount.incrementAndGet();

            // 创建 Worker 和其背后的线程
            Worker worker = new Worker(firstTask);
            Thread newThread = threadFactory.newThread(worker); // 使用 ThreadFactory 创建线程
            worker.thread = newThread;

            try {
                // 将新创建的 worker 添加到工作集中，并启动线程
                workers.add(worker);
                newThread.start();
                return true;
            } catch (Throwable ex) {
                // 【关键的回滚逻辑】
                // 如果添加或启动线程失败 (例如，OutOfMemoryError)，必须进行回滚操作，
                // 否则 workerCount 和 workers 集合的状态会与实际运行的线程数不一致。
                workers.remove(worker);
                workerCount.decrementAndGet();
                log.error("添加工作线程失败", ex); // 使用日志记录失败
                return false;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 处理工作线程退出的清理工作。
     * 当一个 worker 线程的 run() 方法结束时，此方法被调用。
     *
     * @param w 退出的 Worker 对象
     */
    private void processWorkerExit(Worker w) {
        mainLock.lock(); // 必须加锁，因为要修改共享的 workers 集合
        try {
            workers.remove(w);
            workerCount.decrementAndGet(); // 减少工作线程计数
        } finally {
            mainLock.unlock();
        }
    }

    // --- Worker 内部类 ---

    /**
     * Worker 是线程池的核心内部类，它承担了双重角色：
     * 1.  它是一个 Runnable，因此可以被一个 Thread 执行。它的 run() 方法是工作线程的主循环。
     * 2.  它持有一个 Thread 引用 (worker.thread)，使得线程池可以方便地对工作线程进行管理（如中断）。
     * 将任务和执行任务的线程绑定在一起，简化了线程管理。
     */
    private final class Worker implements Runnable {
        /**
         * 这个 worker 的第一个任务。执行完后会置为 null。
         */
        Runnable firstTask;
        /**
         * 执行这个 Worker 的实际线程。在 addWorker 中创建并赋值。
         */
        Thread thread;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        /**
         * run() 方法是工作线程的入口点。它将实际的循环逻辑委托给 runWorker 方法。
         */
        @Override
        public void run() {
            runWorker(this);
        }
    }

    /**
     * 工作线程的主循环。
     * 它会不断地通过 getTask() 方法获取任务并执行，直到 getTask() 返回 null。
     *
     * @param worker 执行此逻辑的 worker
     */
    private void runWorker(Worker worker) {
        Runnable task = worker.firstTask;
        worker.firstTask = null; // 释放对 firstTask 的引用，允许GC回收

        try {
            // 循环：只要 task 不为 null (初始任务或从队列获取的任务)，就一直循环
            while (task != null || (task = getTask()) != null) {
                try {
                    task.run(); // 执行任务
                } catch (RuntimeException e) {
                    // 捕捉任务执行中的异常，防止因单个任务异常导致整个工作线程退出。
                    log.error("任务执行时发生异常", e);
                } finally {
                    task = null; // 任务执行完毕，清空 task 引用，准备获取下一个任务
                }
            }
        } finally {
            // 当 getTask() 返回 null 时，循环会终止，意味着这个 worker 需要退出。
            // 在退出前，必须执行清理工作。
            processWorkerExit(worker);
        }
    }

    /**
     * 工作线程从队列获取任务的核心逻辑。
     * 这个方法的返回值决定了工作线程的生死：
     * - 返回一个 Runnable：线程继续执行任务。
     * - 返回 null：线程将退出主循环并终止。
     *
     * @return 从队列中获取到的任务，或者 null（如果线程需要退出）
     */
    private Runnable getTask() {
        try {
            int rs = runState.get();
            // 场景 1: 检查线程池是否需要停止。
            // 如果状态是 STOP，或者状态是 SHUTDOWN 且队列已空，则没有必要再等待任务了，
            // 工作线程应该立即退出。
            if (rs >= STOP || (rs == SHUTDOWN && workQueue.isEmpty())) {
                return null;
            }

            // 场景 2: 判断当前工作线程是否需要超时退出。
            // 如果当前线程数超过了核心线程数，那么这个线程就是 "非核心线程"，它在空闲时需要有超时限制。
            boolean timed = workerCount.get() > corePoolSize;

            // 场景 3: 根据是否需要超时，选择不同的方法获取任务。
            if (timed) {
                // 对于非核心线程，使用 poll() 进行超时获取。
                // 如果在 keepAliveTime 内没有等到任务，poll() 会返回 null。
                // 这个 null 返回值会传递到 runWorker 的循环判断处，导致循环终止，线程退出。
                // 这就是线程池 "缩容" 的实现机制。
                return workQueue.poll(keepAliveTime, unit);
            } else {
                // 对于核心线程，使用 take() 进行阻塞等待。
                // 它会一直阻塞，直到队列中有新任务或者线程被中断 (shutdownNow)。
                return workQueue.take();
            }
        } catch (InterruptedException e) {
            // 场景 4: 捕获中断异常。
            // 当 shutdownNow() 方法被调用时，它会中断所有线程。
            // 阻塞在 take() 或 poll() 上的线程会抛出 InterruptedException。
            // 捕获此异常后，返回 null，同样可以使线程安全地退出。
            return null;
        }
    }

    /**
     * 默认的线程工厂类。
     * 它用于创建具有统一命名规范的线程，这对于调试和监控非常有用。
     * 例如，创建的线程名可能为 "lbs-pool-1-thread-1", "lbs-pool-1-thread-2" 等。
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            // 为每个线程池实例生成一个唯一的池编号
            this.namePrefix = "lbs-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            // 确保创建的线程不是守护线程。
            // 如果是守护线程，当主线程退出时，线程池的线程也会被强制终止，可能导致任务未完成。
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            // 设置为标准优先级
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}