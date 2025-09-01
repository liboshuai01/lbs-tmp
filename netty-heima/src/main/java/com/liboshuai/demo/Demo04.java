package com.liboshuai.demo;

import java.nio.ByteBuffer;

import static com.liboshuai.demo.ByteBufferUtil.debugAll;

public class Demo04 {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put((byte) 0x61);
        debugAll("one", byteBuffer);
        byteBuffer.put(new byte[]{0x62, 0x63, 0x64});
        debugAll("two", byteBuffer);
        byteBuffer.flip();
        debugAll("three", byteBuffer);
        byte data = byteBuffer.get();
        System.out.println("data: " + (char)data);
        debugAll("four", byteBuffer);
    }
}
