package com.liboshuai.demo.juc.problem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 第九关（最终关）：ThreadLocal 与 上下文传递 (已解答)
 * 场景：Flink Runtime Context 传递，链路追踪
 */
public class ConcurrencyTask09 {

    // 任务 A: 使用 InheritableThreadLocal 可以解决直接 new Thread 的问题
    // 但对线程池无效且危险
    public static ThreadLocal<String> contextHolder = new InheritableThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        // 设置主线程上下文
        contextHolder.set("Main-Context-001");
        System.out.println("主线程: Set Context = " + contextHolder.get());

        // --- 情况 1: 普通子线程 ---
        // 因为用了 InheritableThreadLocal，这里能拿到 001
        Thread childThread = new Thread(() -> {
            System.out.println("Child Thread (New): Get Context = " + contextHolder.get());
        });
        childThread.start();
        childThread.join();

        // --- 情况 2: 线程池 (灾难现场) ---
        ExecutorService threadPool = Executors.newFixedThreadPool(1);

        // 第一次提交：线程池新建线程，复制了 001
        threadPool.submit(() -> {
            System.out.println("ThreadPool-1: Get Context = " + contextHolder.get());
        });

        Thread.sleep(100); // 确保任务1执行完

        // 模拟主线程修改了上下文
        contextHolder.set("Main-Context-002");
        System.out.println("主线程: Update Context = " + contextHolder.get());

        // 任务 B: 第二次提交 (复用同一个线程)
        // 即使主线程改成了 002，这里打印的依然是 001 (脏数据)
        threadPool.submit(() -> {
            System.out.println("ThreadPool-2 (Reuse): Get Context = " + contextHolder.get() + " [错! 应该是002]");
        });

        Thread.sleep(100);

        // --- 任务 C: 正确的做法 (手动快照传递) ---
        // 1. 在主线程提交任务前，捕获当前上下文
        String capturedContext = contextHolder.get();

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                // 2. 在子线程开始执行时，恢复上下文
                String backup = contextHolder.get(); // 备份子线程原有的脏数据(如果有)
                contextHolder.set(capturedContext);  // 注入正确的上下文

                try {
                    System.out.println("ThreadPool-3 (Manual): Get Context = " + contextHolder.get() + " [对!]");
                    // 执行业务逻辑...
                } finally {
                    // 3. 清理现场，防止污染下一次复用
                    contextHolder.set(backup); // 或者直接 remove()
                }
            }
        });

        threadPool.shutdown();
    }

    /*
     * TODO: 思考题
     * 为什么线程池场景下不建议用 InheritableThreadLocal？
     * 答：因为线程池会复用线程。InheritableThreadLocal 只有在线程“创建（new）”的时候才会从父线程拷贝数据。
     * 一旦线程被复用，它不会再次同步父线程的最新数据，反而会持有上一次任务遗留的“脏数据”，导致严重的逻辑错误（串号）。
     */
}
