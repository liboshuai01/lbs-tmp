package com.liboshuai.demo.threadpool;

import java.util.concurrent.RejectedExecutionException;

// 1. 抛出异常策略 (默认)
public class LbsAbortPolicy implements LbsRejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, LbsThreadPoolExecutor executor) {
        throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + executor.toString());
    }
}