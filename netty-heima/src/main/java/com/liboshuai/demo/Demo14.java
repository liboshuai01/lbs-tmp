package com.liboshuai.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Demo14 {
    static class Server {
        public static void main(String[] args) {
            try {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.bind(new InetSocketAddress(8080));
                ssc.configureBlocking(false);
                Selector selector = Selector.open();
                SelectionKey selectionKey = ssc.register(selector, 0, null);
                System.out.println("selectionKey: " + selectionKey);
                selectionKey.interestOps(SelectionKey.OP_ACCEPT);
                ByteBuffer buffer = ByteBuffer.allocate(256);
                while (true) {
                    System.out.println("有感兴趣的事件发生前......");
                    selector.select(); // 阻塞住，直到有感兴趣的事件发生
                    System.out.println("有感兴趣的事件发生了......");
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey next = iterator.next();
                        iterator.remove();
                        if (next.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) next.channel();
                            System.out.println("连接建立前......");
                            SocketChannel sc = serverSocketChannel.accept();
                            System.out.println("连接建立后......");
                            sc.configureBlocking(false);
                            SelectionKey scKey = sc.register(selector, 0, null);
                            scKey.interestOps(SelectionKey.OP_READ);
                        } else if (next.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) next.channel();
                            System.out.println("接收消息前......");
                            socketChannel.read(buffer);
                            System.out.println("接收消息后......");
                            buffer.flip();
                            System.out.println("接收到来自客户端的信息：" + StandardCharsets.UTF_8.decode(buffer));
                            buffer.clear();
                        } else {
                            System.out.println("其他事件...");
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    static class Client {
        public static void main(String[] args) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
                // 打断点
                System.out.println("成功建立与服务端的连接......");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
