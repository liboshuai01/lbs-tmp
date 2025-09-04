package com.liboshuai.demo;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo21 {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(() -> {
            log.info("执行了计算");
            return 10;
        });
        future.addListener((GenericFutureListener<Future<Integer>>) f -> {
            Integer result = f.getNow();
            log.info("得到了计算结果：{}", result);
        });
        log.info("主线程执行到了这里");
    }
}
