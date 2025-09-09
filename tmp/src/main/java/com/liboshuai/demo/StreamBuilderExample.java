package com.liboshuai.demo;

import java.util.stream.Stream;

public class StreamBuilderExample {
    public static void main(String[] args) {
        // 1. 获取 Stream.Builder
        Stream.Builder<String> streamBuilder = Stream.builder();

        // 2. 链式地或逐个添加元素
        streamBuilder.add("Hello")
                .add("Builder")
                .add("Pattern")
                .accept("in JDK 8!"); // accept() 和 add() 效果一样

        // 3. 构建最终的 Stream 对象
        Stream<String> stream = streamBuilder.build();

        // 4. 使用 Stream
        stream.forEach(System.out::println);
    }
}
