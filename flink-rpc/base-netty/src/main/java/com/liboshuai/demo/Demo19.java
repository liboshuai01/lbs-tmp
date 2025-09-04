package com.liboshuai.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Demo19 {
    static class Server {
        public static void main(String[] args) {
            // 1. 启动器，负责组装 Netty 组件，启动服务器
            new ServerBootstrap()
                    // 2. BossEventLoop，WorkerEventLoop（selector，thread），group 组
                    .group(new NioEventLoopGroup())
                    // 3. 选择服务器的 ServerSocketChannel 实现
                    .channel(NioServerSocketChannel.class)
                    // 4. Boss 负责处理连接 worker（child）负责处理读写，决定了 worker（child）能执行哪些操作（handler）
                    .childHandler(
                            // 5. channel 代表和客户端进行数据读写的通道 Initializer 初始化，负责添加别的 handler
                            new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel ch) throws Exception {
                                    // 6. 添加具体 handler
                                    ch.pipeline().addLast(new LoggingHandler());
                                    ch.pipeline().addLast(new StringDecoder());
                                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() { // 自定义 handler

                                        // 读事件
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            log.info("{}", msg); // 打印上一步转换好的字符串
                                        }
                                    });
                                }
                            }
                    )
                    // 7. 绑定监听端口
                    .bind(8080);
        }
    }

    static class Client {
        public static void main(String[] args) throws InterruptedException {
            // 1. 启动类
            new Bootstrap()
                    // 2. 添加 EventLoop
                    .group(new NioEventLoopGroup())
                    // 3. 选择客户端 channel 实现
                    .channel(NioSocketChannel.class)
                    // 4. 添加处理器
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                        }
                    })
                    // 5. 连接到服务器
                    .connect(new InetSocketAddress("localhost", 8080))
                    .sync()
                    .channel()
                    // 6. 向服务器发送数据
                    .writeAndFlush("hello, world");
        }
    }
}
