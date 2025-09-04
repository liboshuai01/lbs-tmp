package com.liboshuai.demo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Demo09 {
    public static void main(String[] args) {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("World");
        ByteBuffer buffer3 = StandardCharsets.UTF_8.encode("你好");
        try (FileChannel fileChannel = new RandomAccessFile("flink-source/base-netty/data/demo09.txt", "rw").getChannel()) {
            long result = fileChannel.write(new ByteBuffer[]{buffer1, buffer2, buffer3});
            System.out.println("result: " + result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
