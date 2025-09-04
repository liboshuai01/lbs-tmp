package com.liboshuai.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Demo13 {
    static class Server {
        public static void main(String[] args) {
            try {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.bind(new InetSocketAddress(8080));
                ssc.configureBlocking(false); // 设置 ServerSocketChannel 为非阻塞，影响 ssc.accept()
                ByteBuffer buffer = ByteBuffer.allocate(256);
                List<SocketChannel> scList = new ArrayList<>();
                while (true) {
                    SocketChannel sc = ssc.accept();
                    if (sc != null) {
                        System.out.println("连接建立成功......");
                        sc.configureBlocking(false); // 设置 SocketChannel 为非阻塞，影响 sc.read()
                        scList.add(sc);
                    }
                    for (SocketChannel socketChannel : scList) {
                        int length = socketChannel.read(buffer);
                        if (length > 0) {
                            buffer.flip();
                            System.out.println("接收到来自客户端的信息：" + StandardCharsets.UTF_8.decode(buffer));
                            buffer.clear();
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
                SocketChannel sc = SocketChannel.open();
                sc.connect(new InetSocketAddress("127.0.0.1", 8080));
                // 这里打断点（手动执行`sc.write(StandardCharsets.UTF_8.encode("333"))`）
                System.out.println("连接建立成功......");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
