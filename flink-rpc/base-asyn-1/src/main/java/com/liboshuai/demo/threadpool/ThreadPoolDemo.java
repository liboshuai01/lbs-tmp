package com.liboshuai.demo.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java 线程池生产环境最佳实践示例
 *
 * All in one file using static inner classes.
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        int corePoolSize = 2;
        int maximumPoolSize = 5;
        long keepAliveTime = 60;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
        ThreadFactory threadFactory = new NamedThreadFactory("processing-pool");
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        System.out.println(">>> 线程池已经配置成功，配置信息如下：>>>");
        System.out.println("核心线程数：" + corePoolSize);
        System.out.println("最大线程数：" + maximumPoolSize);
        System.out.println("线程空闲存活时间：" + keepAliveTime + "秒");
        System.out.println(">>> --------------------------- <<<");

        List<Future<String>> futures = new ArrayList<>();
        int taskCount = 20;
        System.out.printf("准备提交 %d 个订单处理任务...%n", taskCount);
        for (int i = 1; i <= taskCount; i++) {
            ProcessingTask processingTask = new ProcessingTask("Order-" + i);
            Future<String> future = threadPool.submit(processingTask);
            futures.add(future);
        }
        System.out.println("所有任务已提交，等待处理结果...");

        // 遍历Future列表，获取每个任务的结果
        for (int i = 0; i < futures.size(); i++) {
            Future<String> future = futures.get(i);
            try {
                String result = future.get();
                System.out.println("✅ " + result);
            } catch (InterruptedException e) {
                // 当前线程在等待过程中被中断
                Thread.currentThread().interrupt(); // 重新设置中断状态
                System.err.println("❌ 任务 " + (i + 1) + " 的等待过程被中断。");
            } catch (ExecutionException e) {
                // 任务执行过程中抛出了异常
                // e.getCause() 获取真实的异常
                System.err.println("❌ 任务 " + (i + 1) + " 执行失败! 原因: " + e.getCause().getMessage());
            }
        }

        // ================== 4. 优雅关闭线程池 ==================
        System.out.println("所有任务结果已处理，开始关闭线程池...");
        threadPool.shutdown(); // 发起关闭指令，不再接受新任务，但会处理完队列中的任务
        try {
            // 等待线程池在指定时间内终止
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 如果超时还未关闭，则强制关闭
                System.err.println("线程池在60秒内未能优雅关闭，尝试强制关闭...");
                threadPool.shutdownNow();
                // 再次等待，确保资源释放
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("线程池未能终止。");
                }
            }
        } catch (InterruptedException ie) {
            // 在等待关闭过程中当前线程被中断，也尝试强制关闭
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("线程池已关闭。");
        System.out.println("主线程 [" + Thread.currentThread().getName() + "] 结束运行。");
    }

    static class NamedThreadFactory implements ThreadFactory {

        private final String namePrefix;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public NamedThreadFactory(String poolName) {
            this.namePrefix = poolName + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            // 设为非守护线程，如果需要，可以设为守护线程 t.setDaemon(true);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            // 设置标准优先级
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            System.out.println("线程工厂创建了一个新线程: " + t.getName());
            return t;
        }
    }

    static class ProcessingTask implements Callable<String> {

        private final String orderId;

        public ProcessingTask(String orderId) {
            this.orderId = orderId;
        }

        @Override
        public String call() throws Exception {
            String name = Thread.currentThread().getName();
            System.out.printf(">>> 线程 [%s] 开始处理任务 [%s] <<<%n", name, orderId);

            // 模拟业务处理耗时
            long sleepTime = (long) (Math.random() * 2000 + 1000);
            TimeUnit.MILLISECONDS.sleep(sleepTime);

            // 模拟任务失败的场景
            if (Objects.equals(orderId, "Order-7")) {
                throw new IllegalStateException("订单数据格式错误");
            } else if (Objects.equals(orderId, "Order-13")) {
                throw new RuntimeException("数据库连接超时");
            }
            return String.format("线程 [%s] 完成处理任务 [%s]，耗时 [%d]%n", name, orderId, sleepTime);
        }
    }
}