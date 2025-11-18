package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class CheckpointCleaner {

    // 存储 CheckpointID -> Checkpoint元数据
    private final ConcurrentHashMap<Long, String> checkpointStore = new ConcurrentHashMap<>();

    // 初始化一些数据
    public CheckpointCleaner() {
        checkpointStore.put(100L, "State-100");
        checkpointStore.put(101L, "State-101");
        checkpointStore.put(102L, "State-102");
        checkpointStore.put(103L, "State-103");
    }

    /**
     * 清理过期的 Checkpoint
     *
     * @param minRetainId 最小需要保留的 ID (小于这个 ID 的都要删除)
     */
    public void cleanupOldCheckpoints(long minRetainId) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 移除所有 key < minRetainId 的条目
        // 2. 使用 Java 8 的新特性，一行代码完成最好
        // 3. 保证线程安全
        checkpointStore.keySet().removeIf(key -> key < minRetainId);
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        CheckpointCleaner cleaner = new CheckpointCleaner();

        // 我们希望保留 102 及之后的，所以 100 和 101 应该被删掉
        cleaner.cleanupOldCheckpoints(102L);

        System.out.println("Remaining checkpoints: " + cleaner.checkpointStore.keySet());
        // 预期输出: [102, 103] (顺序可能不同)
    }
}
