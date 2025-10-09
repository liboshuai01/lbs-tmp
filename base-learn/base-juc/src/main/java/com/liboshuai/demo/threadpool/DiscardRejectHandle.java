package com.liboshuai.demo.threadpool;


import lombok.extern.slf4j.Slf4j;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Slf4j
public class DiscardRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool threadPool) {
//        threadPool.blockingQueue.poll();
//        threadPool.execute(rejectCommand);
        log.warn("任务被拒绝了");
    }
}
