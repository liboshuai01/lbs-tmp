package com.liboshuai.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

public class Demo25 {
    static class ByteBufCreationDemo {
        public static void main(String[] args) {
            // 1. 创建一个堆内存缓冲区
            ByteBuf heapBuf = ByteBufAllocator.DEFAULT.heapBuffer();
            if (heapBuf.hasArray()) {
                byte[] array = heapBuf.array();
                System.out.println("Heap Buffer 底层数组：" + array);
                System.out.println("数组偏移量：" + heapBuf.arrayOffset());
            }
            System.out.println("Heap Buffer 类型：" + heapBuf.getClass().getSimpleName());
            System.out.println("-----------------------------------------");

            // 2. 创建一个直接内存缓冲区（Direct Buffer）
            // 底层是堆外内存，没有 byte[] 数组
//            ByteBuf directBuffer = ByteBufAllocator.DEFAULT.directBuffer();
            ByteBuf directBuffer = ByteBufAllocator.DEFAULT.buffer();
            if (!directBuffer.hasArray()) {
                System.out.println("Direct Buffer 没有底层数组。");
            }
            System.out.println("Direct Buffer 类型：" + directBuffer.getClass().getSimpleName());
        }
    }

    static class ReadWriteDemo {
        public static void main(String[] args) {
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(16);
            printBufferDetails("初始状态", buffer);

            // 1. 写入数据
            System.out.println("\n写入 'Netty' (5字节) ...");
            buffer.writeBytes("Netty".getBytes(StandardCharsets.UTF_8));
            printBufferDetails("写入后", buffer);

            // 2. 写入一个 int（4字节）
            System.out.println("\n写入 int(123)...");
            buffer.writeInt(123);
            printBufferDetails("再次写入后", buffer);

            // 3. 读取数据
            System.out.println("\n读取 5 个字节...");
            byte[] readBytes = new byte[5];
            buffer.readBytes(readBytes);
            System.out.println("读取到的内容：" + new String(readBytes, StandardCharsets.UTF_8));
            printBufferDetails("读取后", buffer);

            // 4. 读取 int
            System.out.println("\n读取 int...");
            int value = buffer.readInt();
            System.out.println("读取到 int: " + value);
            printBufferDetails("再次读取后", buffer);

            // 5. 演示 clear() 方法
            System.out.println("\n调用 clear() 方法...");
            buffer.clear();
            printBufferDetails("clear() 后", buffer);

        }

        private static void printBufferDetails(String step, ByteBuf buffer) {
            System.out.println("------ " + step + " ------");
            System.out.println("capacity(): " + buffer.capacity());
            System.out.println("readerIndex(): " + buffer.readerIndex());
            System.out.println("writerIndex(): " + buffer.writerIndex());
            System.out.println("readableBytes(): " + buffer.readableBytes());
            System.out.println("writableBytes(): " + buffer.writableBytes());
            System.out.println("isReadable(): " + buffer.isReadable());
            System.out.println("isWritable(): " + buffer.isWritable());
        }
    }

    static class AutoExpansionDemo {
        public static void main(String[] args) {
            // 初始容量为 8
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(8);
            System.out.println("初始容量：" + buffer.capacity());

            System.out.println("\n-----------------------------------------\n");

            // 写入 8 个字节，刚好写满
            buffer.writeBytes(new byte[8]);
            System.out.println("写入 8 字节后容量：" + buffer.capacity());
            System.out.println("可写字节数：" + buffer.writableBytes());

            System.out.println("\n-----------------------------------------\n");

            // 再写入一个字节，此时会触发扩容
            System.out.println("尝试写入第 9 个字节...");
            buffer.writeByte(1);
            System.out.println("写入第 9 字节后容量：" + buffer.capacity());
            System.out.println("可写字节数：" + buffer.writableBytes());
        }
    }

    static class SliceDemo {
        public static void main(String[] args) {
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            buffer.writeCharSequence("Netty in Action", StandardCharsets.UTF_8);
            System.out.println("原始 Buffer 内容：" + buffer.toString(StandardCharsets.UTF_8));

            // 切片，获取从索引 0 开始，长度为 5 的部分（"Netty"）
            ByteBuf sliced = buffer.slice(0, 5);
            System.out.println("Slice Buffer 内容：" + sliced.toString(StandardCharsets.UTF_8));

            // 修改 slice 中的数据
            System.out.println("\n修改 Slice 的第 0 个字节为 'J' ...");
            sliced.setByte(0, 'J');

            // 检查原始 buffer 是否也被修改
            System.out.println("修改后的 Slice 内容：" + sliced.toString(StandardCharsets.UTF_8));
            System.out.println("修改后的原始 Buffer 内容：" + buffer.toString(StandardCharsets.UTF_8));

            // 注意：assert 判断，如果内容不一致会抛出异常
            assert buffer.getByte(0) == sliced.getByte(0);
            System.out.println("\n断言成功：原始 Buffer 和 Slice 共享底层内存。");
        }
    }

    static class ReferenceCountingDemo {
        public static void main(String[] args) {
            // 获取一个池化的使用直接内存的 ByteBuf
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            System.out.println("创建后，refCnt: " + buffer.refCnt());

            // 增加引用计数
            buffer.retain();
            System.out.println("retain() 后，refCnt: " + buffer.refCnt());

            // 减少引用计数
            buffer.release();
            System.out.println("第一次 release() 后，refCnt: " + buffer.refCnt());

            // 再次减少引用计数，此时引用计数为 0，ByteBuf 会被回收
            buffer.release();
            System.out.println("第二次 release() 后，refCnt: " + buffer.refCnt());

            // 尝试访问已经释放的 ByteBuf
            try {
                buffer.writeInt(1);
            } catch (Exception e) {
                System.out.println("\n访问已释放的 Buffer, 捕获到异常: ");
                e.printStackTrace(System.out);
            }

            // 最佳实践：使用 try-finally 结构确保释放
        }
    }

}
