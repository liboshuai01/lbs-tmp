package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class Demo03 {
    public static void main(String[] args) {
        try (FileChannel fileChannel = new FileInputStream("flink-source/base-netty/data/Demo03.txt").getChannel()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (fileChannel.read(byteBuffer) != -1) {
                // 相当与 byteBuffer.flip()
                byteBuffer.limit(byteBuffer.position());
                byteBuffer.position(0);
                // 相当于 byteBuffer.hasRemaining()
                while (byteBuffer.position() < byteBuffer.limit()) {
                    byte data = byteBuffer.get();
                    log.info("{}", (char) data);
                }
                // 相当于 byteBuffer.clear()
                byteBuffer.limit(byteBuffer.capacity());
                byteBuffer.position(0);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
