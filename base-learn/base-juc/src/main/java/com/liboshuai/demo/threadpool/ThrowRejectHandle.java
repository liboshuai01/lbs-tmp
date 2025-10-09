package com.liboshuai.demo.threadpool;


/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ThrowRejectHandle implements RejectHandle {
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool threadPool) {
        throw new RuntimeException("阻塞队列满了！");
    }
}
