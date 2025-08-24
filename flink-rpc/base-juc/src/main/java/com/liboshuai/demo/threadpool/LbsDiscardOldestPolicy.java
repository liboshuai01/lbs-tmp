package com.liboshuai.demo.threadpool;

public class LbsDiscardOldestPolicy implements LbsRejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, LbsThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            executor.getQueue().poll(); // 移除队列头部的任务
            executor.execute(r);      // 重新尝试提交当前任务
        }
    }
}
