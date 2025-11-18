package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class CheckpointAckCounter {

    // 存储 CheckpointID -> 已收到的 Ack 数量
    private final ConcurrentHashMap<Long, Integer> ackCounts = new ConcurrentHashMap<>();

    /**
     * 处理 Checkpoint 确认
     *
     * @param checkpointId Checkpoint 的 ID
     * @return 更新后的 Ack 总数
     */
    public Integer acknowledge(Long checkpointId) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 将 checkpointId 对应的计数加 1
        // 2. 如果不存在，则初始化为 1
        // 3. 返回更新后的值
        return ackCounts.merge(checkpointId, 1, Integer::sum);
//        return null; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        CheckpointAckCounter counter = new CheckpointAckCounter();
        long cpId = 1001L;

        // 模拟多线程并发 Ack
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.acknowledge(cpId);
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 预期结果应该是 2000
        System.out.println("Final count for checkpoint " + cpId + ": " + counter.ackCounts.get(cpId));
    }
}
