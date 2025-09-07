package com.liboshuai.demo.test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 字符串转 ByteBuffer
 */
public class Demo07 {
    public static void main(String[] args) {
        // 1. 方法一
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes());
        ByteBufferUtil.debugAll("方法一", buffer1);
        buffer1.flip();
        String string1 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println("string1: " + string1);

        // 2. 方法二
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        ByteBufferUtil.debugAll("方法二", buffer2);
        String string2 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println("string2: " + string2);

        // 3. 方法三
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        ByteBufferUtil.debugAll("方法三", buffer3);
        String string3 = StandardCharsets.UTF_8.decode(buffer3).toString();
        System.out.println("string3: " + string3);
    }
}
