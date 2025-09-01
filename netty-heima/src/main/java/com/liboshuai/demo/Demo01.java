package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Demo01 {
    public static void main(String[] args) {
        try (FileChannel channel = new FileInputStream("netty-heima/data/demo01.txt").getChannel()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            Charset charset = StandardCharsets.UTF_8;
            while (channel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                CharBuffer charBuffer = charset.decode(byteBuffer);
                log.info("{}", charBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

