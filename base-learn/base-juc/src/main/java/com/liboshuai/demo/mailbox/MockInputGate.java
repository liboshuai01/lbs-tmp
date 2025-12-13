package com.liboshuai.demo.mailbox;


import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 模拟 Flink 的 InputGate。
 * 它是 Netty 线程（生产者）和 StreamTask（消费者）之间的数据交换区。
 */
public class MockInputGate {

    private final Queue<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    // 当有新数据到达时，需要执行的回调（通常是恢复 Task 的执行）
    private Runnable availabilityListener;

    /**
     * [Netty 线程调用] 模拟从网络收到数据
     */
    public void pushData(String data) {
        lock.lock();
        try {
            queue.add(data);
            // 如果 Task 因为没数据挂起了（注册了监听器），现在数据来了，通过监听器唤醒它
            if (availabilityListener != null) {
                Runnable listener = this.availabilityListener;
                this.availabilityListener = null; // 触发一次后移除，避免重复触发
                listener.run();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * [Task 线程调用] 获取数据
     * @return 数据，或者 null (如果没有数据)
     */
    public String poll() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * [Task 线程调用] 注册一个监听器，当有数据到达时调用
     */
    public void registerAvailabilityListener(Runnable listener) {
        lock.lock();
        try {
            if (!queue.isEmpty()) {
                // 如果恰好刚有数据进来，直接执行，不需要等待
                listener.run();
            } else {
                this.availabilityListener = listener;
            }
        } finally {
            lock.unlock();
        }
    }
}
