package com.liboshuai.demo.echo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * Netty实现的非阻塞I/O（NIO）回声客户端
 */
@Slf4j
public class NettyEchoClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8080;
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new EchoClientHandler());
                        }
                    });

            // 启动客户端
            ChannelFuture f = b.connect(host, port).sync();
            log.info("连接至服务器，主机：{}， 端口：{}", host, port);
            log.info("请输入文本（输入exit退出）：");

            // 从控制台读取输入并发送到服务端
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }
                f.channel().writeAndFlush(line);
            }
            // 等待连接关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 自定义的客户端业务处理器
     */
    static class EchoClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
            // 当从服务端接收到数据时被调用
            log.info("接收到连接服务端的响应数据：{}", s);
            log.info("请输入文本（输入exit退出）：");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("", cause);
            ctx.close();
        }
    }
}
