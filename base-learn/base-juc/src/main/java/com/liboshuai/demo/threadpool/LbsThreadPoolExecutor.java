package com.liboshuai.demo.threadpool;

public class LbsThreadPoolExecutor {

    private final int coreSize;
    private final int maxSize;

    public LbsThreadPoolExecutor(int coreSize, int maxSize) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
    }

    void execute(Runnable command) {

    }
}
