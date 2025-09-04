package com.liboshuai.demo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class Demo11 {
    public static void main(String[] args) {
        try (FileChannel sourceFileChannel = new RandomAccessFile("flink-rpc/base-netty/data/demo10_source.txt", "r").getChannel();
             FileChannel targetFileChannel = new RandomAccessFile("flink-rpc/base-netty/data/demo10_target.txt", "rw").getChannel()) {
            sourceFileChannel.transferTo(0, sourceFileChannel.size(), targetFileChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
