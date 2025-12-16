package cn.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单的阻塞式 InputGate.
 * 旧版模型不需要像 Mailbox 那样复杂的 Future/Yield 机制.
 * 因为主线程大部分时间只需要阻塞在数据读取上, 或者阻塞在锁上.
 */
@Slf4j
public class MiniInputGate {
    private final Queue<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();

    public void pushData(String data) {
        lock.lock();
        try {
            queue.add(data);
            available.signal(); // 唤醒正在阻塞等待数据的线程
        } finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞式获取数据
     */
    public String pullNextBlocking() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                available.await();
            }
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }
}
