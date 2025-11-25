package com.liboshuai.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

/**
 * 拒绝策略接口
 * 当线程池无法接受新任务时（队列满且线程数达到最大值），执行的策略
 */
public interface MyRejectPolicy {

    /**
     * 拒绝操作
     * @param task 被拒绝的任务
     * @param executor 线程池本身 (为了能获取队列等信息)
     */
    void reject(Runnable task, MyThreadPool executor);

    // =================== 4种内置的经典策略实现 ===================

    /**
     * 策略1: AbortPolicy (默认策略)
     * 直接抛出运行时异常，阻止系统正常工作
     */
    @Slf4j
    class AbortPolicy implements MyRejectPolicy {
        @Override
        public void reject(Runnable task, MyThreadPool executor) {
            log.error("拒绝策略 Abort: 抛出异常");
            throw new RuntimeException("线程池已满，拒绝任务");
        }
    }

    /**
     * 策略2: DiscardPolicy
     * 默默丢弃任务，不抛出异常，也不执行
     * 适用于对任务丢失不敏感的场景 (如日志收集)
     */
    @Slf4j
    class DiscardPolicy implements MyRejectPolicy {
        @Override
        public void reject(Runnable task, MyThreadPool executor) {
            log.warn("拒绝策略 Discard: 丢弃任务，不予处理");
        }
    }

    /**
     * 策略3: CallerRunsPolicy (调用者运行)
     * 谁提交的任务，谁自己去跑。
     * 这是一个“倒逼”机制，当线程池忙不过来时，通过占用提交者线程（通常是主线程或IO线程）的时间，
     * 变相降低了任务提交的速度，防止系统崩溃。
     */
    @Slf4j
    class CallerRunsPolicy implements MyRejectPolicy {
        @Override
        public void reject(Runnable task, MyThreadPool executor) {
            log.info("拒绝策略 CallerRuns: 线程池忙，由调用者线程[{}]自己执行", Thread.currentThread().getName());
            task.run();
        }
    }

    /**
     * 策略4: DiscardOldestPolicy
     * 丢弃队列里等待最久的一个任务（队头），然后尝试重新提交当前任务。
     * 这种策略为了给新任务腾位置，牺牲了老任务。
     */
    @Slf4j
    class DiscardOldestPolicy implements MyRejectPolicy {
        @Override
        public void reject(Runnable task, MyThreadPool executor) {
            log.warn("拒绝策略 DiscardOldest: 丢弃队列头部老任务，重试提交新任务");
            try {
                // 1. 移除队列头部的任务 (带超时防止死锁，虽然理论上不会)
                executor.getQueue().poll(1, java.util.concurrent.TimeUnit.MILLISECONDS);
                // 2. 再次尝试提交当前任务
                executor.execute(task);
            } catch (InterruptedException e) {
                log.error("DiscardOldest执行期间被中断", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}