package com.liboshuai.demo.threadpool;

public class LbsDiscardPolicy implements LbsRejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, LbsThreadPoolExecutor executor) {
        // 什么都不做，直接丢弃
    }
}