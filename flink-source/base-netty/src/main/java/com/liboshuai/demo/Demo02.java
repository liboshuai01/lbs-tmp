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
public class Demo02 {
    public static void main(String[] args) {
        try (FileChannel fileChannel = new FileInputStream("flink-source/base-netty/data/demo02.txt").getChannel()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            Charset charset = StandardCharsets.UTF_8;
            while (fileChannel.read(byteBuffer) != -1) {
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
