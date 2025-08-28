package com.liboshuai.demo.pool;

import java.util.List;

public interface ThreadPool {

    // 提交任务到线程池
    void execute(Runnable task);

    // 优雅关闭
    void shutdown();

    // 立即关闭
    List<Runnable> shutdownNow();

}
