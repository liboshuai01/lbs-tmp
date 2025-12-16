package cn.liboshuai.demo;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MiniInputGate {

    private final Queue<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();

    public void pushData(String record) {
        lock.lock();
        try {
            queue.add(record);
            available.signal();
        } finally {
            lock.unlock();
        }
    }

    public String pollNext() throws InterruptedException {
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
