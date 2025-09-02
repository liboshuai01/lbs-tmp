package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
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
                while (true) {
                    System.out.println("有感兴趣的事件发生前......");
                    selector.select(); // 阻塞住，直到有感兴趣的事件发生
                    System.out.println("有感兴趣的事件发生了......");
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            System.out.println("连接建立前......");
                            SocketChannel sc = serverSocketChannel.accept();
                            System.out.println("连接建立后......");
                            sc.configureBlocking(false);
                            ByteBuffer buffer = ByteBuffer.allocate(4);
                            SelectionKey scKey = sc.register(selector, 0, buffer);
                            scKey.interestOps(SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            System.out.println("接收消息前......");
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            int length = 0;
                            try {
                                length = socketChannel.read(buffer);
                            } catch (IOException e) {
                                log.error("客户端异常断开连接", e);
                                key.cancel();
                            }
                            if (length == -1) {
                                // 如果读取到的数据长度为-1，则表示客户端正常断开，需要手动取消key
                                System.out.println("客户端正常断开连接");
                                key.cancel();
                            } else {
                                System.out.println("接收消息后......");
                                ByteBufferUtil.split(buffer);
                                if (buffer.position() == buffer.limit()) {
                                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                    buffer.flip();
                                    newBuffer.put(buffer);
                                    key.attach(newBuffer);
                                }
                            }
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
