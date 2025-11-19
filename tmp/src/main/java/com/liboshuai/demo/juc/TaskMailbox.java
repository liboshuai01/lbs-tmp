package com.liboshuai.demo.juc;

/**
 * 邮箱接口，用于存放待执行的动作（Runnable 即 "信件"）
 */
public interface TaskMailbox {

    /**
     * 往邮箱里投递一封信（动作）
     * 这通常由外部线程（如 RPC 线程、Timer 线程）调用
     */
    void put(Runnable mail) throws InterruptedException;

    /**
     * 从邮箱里取出一封信
     * 这通常由 Task 主线程调用
     */
    Runnable take() throws InterruptedException;

    /**
     * 关闭邮箱
     */
    void close();
}