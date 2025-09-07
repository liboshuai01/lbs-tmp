package com.liboshuai.demo.test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Demo10 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        buffer.put("你先试一下\n哈哈哈哈\n这东西".getBytes(StandardCharsets.UTF_8));
        ByteBufferUtil.split(buffer);
        buffer.put("我只卖不碰的\n对不住啦\n".getBytes(StandardCharsets.UTF_8));
        ByteBufferUtil.split(buffer);
    }
}
