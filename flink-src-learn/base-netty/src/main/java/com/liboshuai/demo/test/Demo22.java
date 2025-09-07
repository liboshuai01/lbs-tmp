package com.liboshuai.demo.test;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class Demo22 {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(() -> {
            log.info("开始计算啦...");
            try {
                int i = 1 /0 ;
                promise.setSuccess(i);
            } catch (Exception e) {
                promise.setFailure(e);
            }
        }).start();
        log.info("等待计算结果");
        try {
            log.info("获取计算结果: {}", promise.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
