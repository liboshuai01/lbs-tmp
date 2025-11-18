package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class SlotManagerSimulator {

    // 存储 SlotID -> ConnectionID (表示该 Slot 被哪个连接占用了)
    private final ConcurrentHashMap<String, String> allocatedSlots = new ConcurrentHashMap<>();

    // 初始化数据
    public SlotManagerSimulator() {
        allocatedSlots.put("slot-1", "conn-A");
        allocatedSlots.put("slot-2", "conn-B");
    }

    /**
     * 安全释放 Slot
     *
     * @param slotId               Slot 的 ID
     * @param expectedConnectionId 期望的连接 ID (只有匹配这个 ID 才允许删除)
     * @return 是否成功移除
     */
    public boolean freeSlotSafely(String slotId, String expectedConnectionId) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 只有当 map.get(slotId) 等于 expectedConnectionId 时，才移除该条目
        // 2. 返回是否移除成功
        // 3. 必须原子操作
        return allocatedSlots.remove(slotId, expectedConnectionId);
//        return false; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        SlotManagerSimulator sm = new SlotManagerSimulator();

        // 场景 1: 连接 ID 匹配，应该移除成功
        boolean success1 = sm.freeSlotSafely("slot-1", "conn-A");
        System.out.println("Free slot-1 result: " + success1); // 预期: true
        System.out.println("Slot-1 exists? " + sm.allocatedSlots.containsKey("slot-1")); // 预期: false

        // 场景 2: 连接 ID 不匹配 (模拟 Slot 已经被分配给了别人)，应该失败
        boolean success2 = sm.freeSlotSafely("slot-2", "conn-X"); // 期望是 conn-B，但传入 conn-X
        System.out.println("Free slot-2 result: " + success2); // 预期: false
        System.out.println("Slot-2 value: " + sm.allocatedSlots.get("slot-2")); // 预期: 仍然是 conn-B
    }
}
