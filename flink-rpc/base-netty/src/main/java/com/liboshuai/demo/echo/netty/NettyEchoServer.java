package com.liboshuai.demo.echo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty 实现的非阻塞I/O（NIO）回声服务器
 */
@Slf4j
public class NettyEchoServer {
    public static void main(String[] args) {
        int port = 8080;

        // 1. 创建两个 EventLoopGroup: boosGroup 和 workerGroup
        // bossGroup 仅用于处理客户端的连接请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // workerGroup 用于处理已连接客户端的读写操作
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(); // 默认线程数是CPU核心数*2

        try {
            // 2. 创建服务端启动引导类 ServerBootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) // 设置主从线程组
                    .channel(NioServerSocketChannel.class) // 指定使用 NIO 的 ServerSocketChannel
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置 TCP 连接的缓冲区大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持连接活动状态
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 设置 workerGroup 的处理器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // 添加编译码器，Netty 会自动处理字符串与字节的转换
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            // 添加自定义的业务处理器
                            p.addLast(new EchoServerHandler());
                        }
                    });

            // 3. 绑定端口，启动服务器，并同步等待成功
            ChannelFuture f = b.bind(port).sync();
            log.info("Netty回显服务器已经启动，端口：{}", port);
            // 4. 等待服务器的监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务器处理连接时出现异常", e);
        } finally {
            // 5. 优雅地关闭 EventLoopGroup，释放所有资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 自定义服务端业务处理器
     */
    @ChannelHandler.Sharable // @Sharable 表示该 Handler 可以被多个 Channel 共享
    static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 当有数据可读时，此方法被调用。msg 是经过解码后的字符串
            String received = (String) msg;
            log.info("接收到来自客户端的信息：{}", received);
            // 将接收到的消息写回客户端，注意这里不需要.flush()
            // Netty的Encoder会自动处理flush
            ctx.writeAndFlush(received);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 异常处理逻辑
            log.error("出现异常", cause);
            ctx.close(); // 出现异常时关闭连接
        }
    }
}
