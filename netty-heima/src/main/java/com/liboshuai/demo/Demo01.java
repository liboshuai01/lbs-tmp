package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class Demo01 {
    public static void main(String[] args) {
        try (FileInputStream fileInputStream = new FileInputStream("netty-heima/data/demo01.txt")) {
            FileChannel fileChannel = fileInputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (true) {
                int length = fileChannel.read(byteBuffer);
                if (length == -1) {
                    break;
                }
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    byte data = byteBuffer.get();
                    log.info("{}", (char) data);
                }
                byteBuffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

