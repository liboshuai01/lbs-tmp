package com.liboshuai.demo.test;

import java.nio.ByteBuffer;

public class Demo05 {
    public static void main(String[] args) {
//        System.out.println(ByteBuffer.allocate(16).getClass());
//        System.out.println(ByteBuffer.allocateDirect(16).getClass());
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16);
        byteBuffer.put(new byte[]{0x61, 0x62, 0x63, 0x64, 0x65});

        byteBuffer.flip();
        byte data1 = byteBuffer.get();
        System.out.println("data1: " + (char) data1);
        byte data2 = byteBuffer.get();
        System.out.println("data2: " + (char) data2);
        byteBuffer.mark();
        byte data3 = byteBuffer.get();
        System.out.println("data3: " + (char) data3);
        byte data4 = byteBuffer.get();
        System.out.println("data4: " + (char) data4);
        byteBuffer.reset();
        byte data5 = byteBuffer.get();
        System.out.println("data5: " + (char) data5);
        byte data6 = byteBuffer.get();
        System.out.println("data6: " + (char) data6);
    }
}
