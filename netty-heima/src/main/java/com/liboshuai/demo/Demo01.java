package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class Demo01 {
    public static void main(String[] args) {
        try (FileChannel channel = new FileInputStream("netty-heima/data/demo01.txt").getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                int length = channel.read(buffer);
                if (length == -1) {
                    break;
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte data = buffer.get();
                    log.info("{}", (char) data);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

