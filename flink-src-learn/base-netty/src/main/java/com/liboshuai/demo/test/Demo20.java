package com.liboshuai.demo.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class Demo20 {
    static class Server {
        public static void main(String[] args) {
            NioEventLoopGroup boss = new NioEventLoopGroup();
            NioEventLoopGroup worker = new NioEventLoopGroup(2);
            DefaultEventLoopGroup group = new DefaultEventLoopGroup(2);
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap()
                        .group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                                ch.pipeline().addLast(new StringDecoder());
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("Handler 1 (I/O 线程) 收到消息: {}", msg);
                                        ctx.fireChannelRead(msg);
                                    }
                                });
                                ch.pipeline().addLast(group, "业务线程组", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("Handler 2 (业务线程) 收到消息: {}", msg);
                                    }
                                });
                            }
                        });
                Channel channel = serverBootstrap.bind(8080).sync().channel();
                log.info("服务器启动成功，端口[8080]");
                channel.closeFuture().sync(); // 阻塞主线程，防止因为主线程结束导致jvm提前退出

            } catch (InterruptedException e) {
                log.error("服务器被中断", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("正在关闭服务器...");
                boss.shutdownGracefully();
                worker.shutdownGracefully();
                group.shutdownGracefully();
            }

        }
    }

    static class Client {
        public static void main(String[] args) {
            NioEventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                                ch.pipeline().addLast(new StringEncoder());
                            }
                        });
                Channel channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
                log.info("成功连接到服务器-[{}]", channel.remoteAddress());
                new Thread(() -> {
                    log.info("请输入文本（exit退出）:");
                    Scanner scanner = new Scanner(System.in);
                    while (scanner.hasNextLine()) {
                        String message = scanner.nextLine();
                        if ("exit".equalsIgnoreCase(message)) {
                            log.info("用户主动关闭了客户端");
                            channel.close();
                            break;
                        }
                        channel.writeAndFlush(message);
                    }
                }, "控制台线程").start();
                channel.closeFuture().sync(); // 阻塞主线程，等待用户主动关闭客户端
            } catch (InterruptedException e) {
                log.error("客户端被中断", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("正在关闭客户端");
                group.shutdownGracefully();
            }
        }
    }
}
