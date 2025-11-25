package cn.liboshuai.jrisk.core.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 有界队列工厂
 */
public class BoundedQueueFactory {

    /**
     * 创建一个有界阻塞队列
     * @param capacity 队列容量 (例如 10000)
     */
    public static <T> BlockingQueue<T> createQueue(int capacity) {
        // false 表示不保证公平性 (吞吐量优先)
        return new ArrayBlockingQueue<>(capacity, false);
    }
}