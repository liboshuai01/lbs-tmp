package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class Demo08 {
    public static void main(String[] args) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("netty-heima/data/demo08.txt", "r")) {
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer buffer1 = ByteBuffer.allocate(5);
            ByteBuffer buffer2 = ByteBuffer.allocate(5);
            ByteBuffer buffer3 = ByteBuffer.allocate(4);
            long ignore = fileChannel.read(new ByteBuffer[]{buffer1, buffer2, buffer3});
            ByteBufferUtil.debugAll("buffer1", buffer1);
            ByteBufferUtil.debugAll("buffer2", buffer2);
            ByteBufferUtil.debugAll("buffer3", buffer3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
