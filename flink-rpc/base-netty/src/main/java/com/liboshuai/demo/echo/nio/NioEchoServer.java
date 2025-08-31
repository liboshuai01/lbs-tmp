package com.liboshuai.demo.echo.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioEchoServer {

    public static void main(String[] args) {
        // 修正: 为服务器级别的核心资源使用 try-with-resources
        // 这样可以确保无论程序如何退出，selector 和 serverSocketChannel 都会被正确关闭
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(8080));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO回声服务器已启动，端口：8080");

            while (true) {
                if (selector.select() == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    // 将单个key的处理逻辑也放在try-catch中，防止某个连接的问题影响整个服务器
                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key, selector);
                        } else if (key.isReadable()) {
                            handleReadAndWrite(key);
                        }
                    } catch (IOException e) {
                        System.err.println("处理客户端连接时出现IO异常：" + e.getMessage());
                        // 当出现异常时，取消key并关闭相关的channel
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException ex) {
                            System.err.println("关闭异常channel时出错：" + ex.getMessage());
                        }
                    } finally {
                        // 处理完事件后，必须手动将其从集合中移除
                        keyIterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("服务器主循环出现严重异常", e);
        }
    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        try (
                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel()
        ) {
            // clientChannel 是一个长生命周期的资源，由selector管理，不能使用try-with-resources
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                clientChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("接受到新的客户端连接：" + clientChannel.getRemoteAddress());
            }
        }
    }

    private static void handleReadAndWrite(SelectionKey key) throws IOException {
        // 注意：这里也不使用 try-with-resources，因为 channel 的生命周期由我们手动控制
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip();
            String receivedMessage = StandardCharsets.UTF_8.decode(buffer).toString();
            System.out.println("接收到来自 " + clientChannel.getRemoteAddress() + " 的信息：" + receivedMessage.trim());

            buffer.rewind();
            clientChannel.write(buffer);
        } else if (bytesRead == -1) {
            // 客户端正常关闭
            System.out.println("客户端 " + clientChannel.getRemoteAddress() + " 已断开连接");
            key.cancel();
            clientChannel.close();
        }
    }
}