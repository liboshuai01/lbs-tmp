package com.liboshuai.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
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
                while (true) {
                    System.out.println("有感兴趣的事件发生前......");
                    selector.select(); // 阻塞住，直到有感兴趣的事件发生
                    System.out.println("有感兴趣的事件发生了......");
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey next = iterator.next();
                        if (next.interestOps() == SelectionKey.OP_ACCEPT) {
                            System.out.println("next: " + next);
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) next.channel();
                            SocketChannel sc = serverSocketChannel.accept();
                            System.out.println("sc: " + sc);
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
                System.out.println("成功建立与服务端的连接......");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
