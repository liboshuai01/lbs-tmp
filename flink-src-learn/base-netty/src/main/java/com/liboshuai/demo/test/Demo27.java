package com.liboshuai.demo.test;

import com.google.common.net.HttpHeaders;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Demo27 {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        NioEventLoopGroup boss = null;
        NioEventLoopGroup worker = null;
        try {
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup(2);
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpRequestHandler());
                            pipeline.addLast(new HttpContentHandler());
                        }
                    });
            Channel channel = serverBootstrap.bind(new InetSocketAddress(HOST, PORT)).sync().channel();
            log.info("Http服务器已经成功启动，地址：{}", HOST + ":" + PORT);
            channel.closeFuture().sync();
            log.info("Http服务器正在关闭...");
        } catch (InterruptedException e) {
            log.error("服务器被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (worker != null) {
                worker.shutdownGracefully();
            }
        }
    }

    static class HttpRequestHandler extends SimpleChannelInboundHandler<HttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) {
            log.info("HttpRequestHandler接收到的数据：{}", httpRequest);
            // 构建响应信息
            String response = "这是Netty响应给你的数据哦！";
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
            httpResponse.headers().setInt(HttpHeaders.CONTENT_LENGTH, response.getBytes(StandardCharsets.UTF_8).length);
            httpResponse.content().writeCharSequence(response, StandardCharsets.UTF_8);
            ctx.writeAndFlush(httpResponse);
        }
    }

    static class HttpContentHandler extends SimpleChannelInboundHandler<HttpContent> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpContent httpContent) {
            ByteBuf myBuffer = ctx.alloc().buffer();
            try {
                myBuffer.writeCharSequence("演示", StandardCharsets.UTF_8);
                log.info("buffer: {}", myBuffer);
            } finally {
                myBuffer.release();
            }
            ByteBuf httpContentByteBuf = httpContent.content();
            String content = httpContentByteBuf.toString(StandardCharsets.UTF_8);
            log.info("HttpContentHandler接收的数据：{}", content);
        }
    }
}
