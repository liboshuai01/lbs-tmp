package com.liboshuai.demo.chat.server;

import com.liboshuai.demo.chat.protocol.MessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ChatServer {

    private static final int PORT = 8080;

    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

    public static void main(String[] args) {


        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(2);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                            pipeline.addLast(LOGGING_HANDLER);
                            pipeline.addLast(MessageCodec.INSTANCE);
                        }
                    });
            Channel channel = serverBootstrap.bind(PORT).sync().channel();
            log.info("服务器启动成功，端口: [{}]", PORT);
            channel.closeFuture().sync(); // 阻塞等待关闭服务器
        } catch (InterruptedException e) {
            log.error("服务器被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("服务器正在关闭...");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
