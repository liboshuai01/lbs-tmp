package com.liboshuai.demo.juc;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicTrafficCounter {

    // TODO: 定义原子变量
    private final AtomicLong count = new AtomicLong(0);

    // 增加计数
    public void increment() {
        // TODO: 实现原子自增
        count.incrementAndGet();
    }

    // 获取当前值
    public long get() {
        // TODO: 获取值
        return count.get();
    }

    // 思考题：
    // 如果有 1000 个线程同时疯狂调用 increment()，
    // AtomicLong 底层的 CAS 会发生什么现象？会对 CPU 造成什么影响？
    /*
    回答:
        会发生同一时间只有一个线程可以成功的完成CAS操作, 使值完成真正的+1操作.
        其他线程会因为值已经发生了变化, CAS操作失败, 而进入下一次CAS操作.
        会让CPU进行大量的循环判断计算, 增加CPU的消耗.
     */
}