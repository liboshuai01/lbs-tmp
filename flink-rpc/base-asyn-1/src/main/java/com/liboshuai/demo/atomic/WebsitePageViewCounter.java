package com.liboshuai.demo.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUC 基本类型原子类 AtomicInteger 业务场景示例
 *
 * 场景：模拟网站某个页面的实时访问量 (PV) 计数器
 * 在高并发下，多个用户（线程）同时访问该页面，我们需要一个线程安全的计数器来准确记录总访问量。
 */
public class WebsitePageViewCounter {
    // 1. 定义一个原子整数，作为我们页面的总访问量计数器，并初始化为0
    // 使用 AtomicInteger 来代替普通的 int，以保证多线程环境下的原子性操作
    private static final AtomicInteger pageViewCount = new AtomicInteger(0);

    /**
     * 模拟用户访问页面的操作
     * 每次调用这个方法，代表有一个用户访问了该页面
     */
    public void handlePageView() {
        // 2. 对访问量进行原子性的自增操作
        // getAndIncrement() 方法会以原子方式将当前值加 1，并返回旧值。
        // 这等价与线程安全的 i++ 操作。
        pageViewCount.getAndIncrement();
        // 如果你不需要返回旧值，可以直接使用 incrementAndGet()，它返回新值。
    }

    /**
     * 获取当前总的页面访问量
     */
    public int getPageViewCount() {
        // 3. 读取当前的计数值
        // get() 方法可以安全地获取当前最新的值。
        return pageViewCount.get();
    }

    /**
     * main方法，用于模拟多线程并发访问的场景。
     */
    public static void main(String[] args) throws InterruptedException {
        // 创建一个页面计数器实例
        WebsitePageViewCounter websitePageViewCounter = new WebsitePageViewCounter();

        // 设定模拟的并发用户数
        final int numberOfUsers = 1000;

        // 使用线程池来模拟1000个用户并发访问
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        System.out.println("开始模拟 " + numberOfUsers + " 个用户并发访问...");

        // 模拟 1000 个用户在短时间内同时访问页面
        for (int i = 0; i < numberOfUsers; i++) {
            threadPool.execute(websitePageViewCounter::handlePageView);
        }

        // 关闭线程池，不再接收任务，但会等待已提交的任务执行完毕
        threadPool.shutdown();
        // 当代所有任务执行完毕，这里设置一个最长等待时间
        threadPool.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("------------------------------------");
        System.out.println("所有模拟用户访问完成。");
        // 输出最终的页面总访问量
        System.out.println("最终页面总访问量 (PV): " + websitePageViewCounter.getPageViewCount());
        System.out.println("预期页面总访问量 (PV): " + numberOfUsers);
    }
}