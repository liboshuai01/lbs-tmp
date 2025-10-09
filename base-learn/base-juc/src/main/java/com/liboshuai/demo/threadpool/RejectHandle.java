package com.liboshuai.demo.threadpool;


/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface RejectHandle {

    void reject(Runnable rejectCommand, MyThreadPool threadPool);
}
