package com.liboshuai.demo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Demo10 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        buffer.put("你先试一下\n哈哈哈哈\n这东西".getBytes(StandardCharsets.UTF_8));
        split(buffer);
        buffer.put("我只卖不碰的\n对不住啦\n".getBytes(StandardCharsets.UTF_8));
        split(buffer);
    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();
        for (int i = 0; i < buffer.limit(); i++) {
            if ((char) buffer.get(i) == '\n') {
                int oldLimit = buffer.limit();
                buffer.limit(i);
                ByteBuffer tmpBuffer = buffer.asReadOnlyBuffer();
                System.out.println(StandardCharsets.UTF_8.decode(tmpBuffer));
                buffer.limit(oldLimit);
                buffer.position(i + 1);
            }
        }
        buffer.compact();
    }
}
