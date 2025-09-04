package com.liboshuai.demo.threadpool;

public interface LbsRejectedExecutionHandler {
    /**
     * 当任务被拒绝时调用的方法
     * @param r 请求执行的任务
     * @param executor 尝试执行此任务的线程池
     */
    void rejectedExecution(Runnable r, LbsThreadPoolExecutor executor);
}