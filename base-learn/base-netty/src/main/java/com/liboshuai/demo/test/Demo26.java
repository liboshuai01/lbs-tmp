package com.liboshuai.demo.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Demo26 {

    // --- 配置区 ---
    // 将主机、端口和密码作为可配置的常量，便于修改
    private static final String HOST = "test";
    private static final int PORT = 6380;
    private static final String PASSWORD = "YOUR_PASSWORD";

    private static ExecutorService threadPool;
    private static final String LINE = "\r\n";

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) {
                            channel.pipeline().addLast(new RedisClientHandler());
                        }
                    });
            // 阻塞等待连接 Redis 服务器
            Channel channel = bootstrap.connect(new InetSocketAddress(HOST, PORT)).sync().channel();
            log.info("成功连接至 Redis 服务器 [{}]", HOST + ":" + PASSWORD);
            // 启动用户输入异步任务
            threadPool = Executors.newFixedThreadPool(1);
            threadPool.execute(new UserInput(channel));
            // 阻塞等待用户主动退出客户端
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("客户端被中断",e);
            Thread.currentThread().interrupt();
        }catch (Exception e) {
            log.error("连接至 [{}] Redis 服务器失败", HOST + ":" + PASSWORD, e);
        } finally {
            log.info("正在关闭 Redis 客户端...");
            group.shutdownGracefully();
            threadPool.shutdown();
        }
    }

    static class RedisClientHandler extends ChannelDuplexHandler {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String authCommand = "AUTH " + PASSWORD;
            ByteBuf buffer = ctx.alloc().buffer();
            boolean success = buildCommand(buffer, authCommand);
            if (success) {
                ctx.writeAndFlush(buffer);
            }
            log.debug("正在进行密码认证...");
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            String response = byteBuf.toString(StandardCharsets.UTF_8);
            log.info("<< Redis 响应: {}", response);
            byteBuf.release();
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            String command = (String ) msg;
            ByteBuf buffer = ctx.alloc().buffer();
            boolean success = buildCommand(buffer, command);
            if (success) {
                super.write(ctx, buffer, promise);
            }
        }

        private boolean buildCommand(ByteBuf byteBuf, String command) {
            int length = command.length();
            if (length < 1) {
                log.error("命令格式错误：个数不能小于1");
                return false;
            }
            String[] commands = command.trim().split("\\s+");
            StringBuilder sb = new StringBuilder("*").append(commands.length).append(LINE);
            for (String cmdPart : commands) {
                sb.append("$").append(cmdPart.length()).append(LINE).append(cmdPart).append(LINE);
            }

            String finalCommand = sb.toString();
            byteBuf.writeCharSequence(finalCommand, StandardCharsets.UTF_8);
            // 将 \r\n 替换为字符串 "\\r\\n" 以便在日志中直接显示，而不是换行
            log.debug("最终发送的Redis命令内容：[{}]", finalCommand.replace("\r\n", "\\r\\n"));
            return true;
        }
    }

    static class UserInput implements Runnable {

        private final Channel channel;

        UserInput(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (channel.isActive()) {
                    log.info("请输入 Redis 命令（exit退出）：");
                    String command = scanner.nextLine();
                    if (command == null || command.isEmpty()) {
                        log.warn("命令格式错误：内容不能为空");
                        continue;
                    }
                    if (command.equalsIgnoreCase("exit")) {
                        channel.close();
                        break;
                    }
                    channel.writeAndFlush(command);
                }
            }
        }
    }
}
