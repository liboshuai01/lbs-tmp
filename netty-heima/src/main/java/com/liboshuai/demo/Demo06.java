package com.liboshuai.demo;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import static com.liboshuai.demo.ByteBufferUtil.debugAll;

public class Demo06 {
    public static void main(String[] args) {
        // 1. 创建并填充一个原始的 ByteBuffer
        ByteBuffer originalBuffer = ByteBuffer.allocate(10);
        debugAll("原始缓冲区（填充前）", originalBuffer);

        // 填充 0 到 9 十个字节
        originalBuffer.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x70});

        debugAll("原始缓冲区（填充后）", originalBuffer);

        // 准备进行操作，先切换到读模式
        originalBuffer.flip();
        debugAll("原始缓冲区（flip后）", originalBuffer);

        // 2. 演示 slice() 方法
        demonstrateSlice(originalBuffer);

        // 3. 演示 duplicate() 方法
        demonstrateDuplicate(originalBuffer);

        // 4. 演示 asReadOnlyBuffer() 方法
        demonstrateAsReadOnlyBuffer(originalBuffer);
    }

    /**
     * 演示 slice() 方法
     * slice() 创建一个从原始缓冲区 position 到 limit 的新缓冲区
     */
    private static void demonstrateSlice(ByteBuffer original) {
        System.out.println();
        System.out.println("+--------+-------------------- 演示 slice() ------------------------+----------------+");
        System.out.println();

        // 在操作前，先保存原始缓冲区的状态，以便后续恢复
        original.mark();

        // 设定一个区域 [3,7) 来创建切片
        original.position(3).limit(7);
        debugAll("原始缓冲区 (设定切片区域后)", original);

        // 创建切片
        ByteBuffer sliceBuffer = original.slice();
        debugAll("切片缓冲区 (创建后)", sliceBuffer);

        // 修改切片缓冲区的内容
        sliceBuffer.put(0, (byte) 0x72);
        sliceBuffer.put(1, (byte) 0x73);
        sliceBuffer.put(2, (byte) 0x74);
        sliceBuffer.put(3, (byte) 0x75);

        debugAll("切片缓冲区内容 (修改后)", sliceBuffer);

        original.reset();
        original.limit(original.capacity());

        debugAll("原始缓冲区 (恢复状态后)", original);
        System.out.println("可以看到，原始缓冲区中索引 3, 4, 5, 6 的内容已经被修改。");
    }

    /**
     * 演示 duplicate() 方法
     * duplicate() 创建一个共享数据但状态独立的完整副本
     */
    private static void demonstrateDuplicate(ByteBuffer original) {
        System.out.println();
        System.out.println("+--------+-------------------- 演示 duplicate() ------------------------+----------------+");
        System.out.println();

        // 将原始缓冲区的状态复原到 flip 之后
        original.clear();
        original.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x70});
        original.flip();

        debugAll("原始缓冲区（重置后）", original);

        // 创建副本
        ByteBuffer duplicateBuffer = original.duplicate();
        debugAll("副本缓冲区（创建后）", duplicateBuffer);

        // 修改副本缓冲区的位置，不会影响原始缓冲区
        duplicateBuffer.position(5);
        debugAll("原始缓冲区（状态不变）", original);
        debugAll("副本缓冲区（位置改变）", duplicateBuffer);

        // 修改副本缓冲区的数据，会影响原始缓冲区
        duplicateBuffer.put(8, (byte) 0x72);
        debugAll("原始缓冲区（数据改变）", original);
        debugAll("副本缓冲区（数据改变）", duplicateBuffer);
        System.out.println("可以看到，原始缓冲区的内容也被修改了。");
    }

    /**
     * 演示 asReadOnlyBuffer() 方法
     * asReadOnlyBuffer() 创建一个只读的视图
     */
    private static void demonstrateAsReadOnlyBuffer(ByteBuffer original) {
        System.out.println();
        System.out.println("+--------+-------------------- 演示 asReadOnlyBuffer() ------------------------+----------------+");
        System.out.println();

        // 将原始缓冲区的状态复原到 flip 之后
        original.clear();
        original.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x70});
        original.flip();

        // 创建只读缓冲区
        ByteBuffer readOnlyBuffer = original.asReadOnlyBuffer();
        debugAll("只读缓冲区（创建后）", readOnlyBuffer);

        // 读取是允许的，而且允许改变position的值
        byte b1 = readOnlyBuffer.get();
        System.out.println("\n从只读缓冲区读取索引0的数据: " + (char) b1);
        debugAll("只读缓冲区（读取索引0的数据后）", readOnlyBuffer);

        // 写入是不允许的
        System.out.println("尝试从只读缓冲区写入数据...");
        try {
            readOnlyBuffer.put(0, (byte) 0x73);
        } catch (ReadOnlyBufferException e) {
            System.out.println("成功捕获异常: " + e.getClass().getName());
            System.out.println("这证明了该缓冲区是只读的。");
        }
    }
}
