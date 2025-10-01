package com.liboshuai.demo.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Demo12 {

    static class Server {
        public static void main(String[] args) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(256);
                // 1. 创建服务器
                ServerSocketChannel ssc = ServerSocketChannel.open();
                // 2. 绑定端口
                ssc.bind(new InetSocketAddress(8080));
                // 3. 创建连接集合
                List<SocketChannel> scList = new ArrayList<>();
                while (true) {
                    // 4. 等待建立连接，以便获取与客户端通信的 SocketChannel
                    System.out.println("等待连接建立前......");
                    SocketChannel sc = ssc.accept(); // 默认为阻塞
                    System.out.println("连接成功被建立......");
                    scList.add(sc);
                    for (SocketChannel socketChannel : scList) {
                        // 5. 接收到客户端发送的数据
                        System.out.println("等待接收数据中......");
                        socketChannel.read(buffer); // 默认为阻塞
                        buffer.flip();
                        System.out.println("接收到来自客户端的信息：" + StandardCharsets.UTF_8.decode(buffer));
                        buffer.clear();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class client {
        public static void main(String[] args) {
            try {
                SocketChannel sc = SocketChannel.open();
                sc.connect(new InetSocketAddress("127.0.0.1", 8080));
                // 打断点
                System.out.println("已经与服务端建立了连接......");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
