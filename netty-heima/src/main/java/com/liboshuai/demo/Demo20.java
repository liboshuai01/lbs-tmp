package com.liboshuai.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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
            new ServerBootstrap()
                    .group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.info("message1: {}", msg);
                                    ctx.fireChannelRead(msg);
                                }
                            });
                            ch.pipeline().addLast(group, "普通工人", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.info("message2: {}", msg);
                                    ctx.fireChannelRead(msg);
                                }
                            });
                        }
                    })
                    .bind(8080);

        }
    }

    static class Client {
        public static void main(String[] args) {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                        }
                    }).connect(new InetSocketAddress("127.0.0.1", 8080));
            channelFuture.addListener((ChannelFutureListener) cf -> {
                log.info("请输入文字（exit退出）");
                Channel channel = cf.channel();
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(message)) {
                        break;
                    }
                    channel.writeAndFlush(message);
                }
                log.info("退出聊天成功");
            });
        }
    }
}
