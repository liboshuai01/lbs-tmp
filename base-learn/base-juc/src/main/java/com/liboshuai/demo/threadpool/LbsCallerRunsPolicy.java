package com.liboshuai.demo.threadpool;

public class LbsCallerRunsPolicy implements LbsRejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, LbsThreadPoolExecutor executor) {
        // 如果线程池未关闭，则由提交任务的线程直接执行
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}