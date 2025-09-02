package com.liboshuai.demo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Demo15 {
    static class Server {
        public static void main(String[] args) {
            try {
                Selector selector = Selector.open();
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.bind(new InetSocketAddress(8080));
                ssc.configureBlocking(false);
                ssc.register(selector, SelectionKey.OP_ACCEPT);
                while (true) {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            ServerSocketChannel sscChannel = (ServerSocketChannel) key.channel();
                            SocketChannel sc = sscChannel.accept();
                            sc.configureBlocking(false);
                            SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < 300000000; i++) {
                                sb.append("a");
                            }
                            ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());
                            int length = sc.write(buffer);
                            System.out.println("length1: " + length);
                            if (buffer.hasRemaining()) {
                                scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                                scKey.attach(buffer);
                            }
                        } else if (key.isWritable()) {
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            SocketChannel sc = (SocketChannel) key.channel();
                            int length = sc.write(buffer);
                            System.out.println("length2: " + length);
                            if (!buffer.hasRemaining()) {
                                key.attach(null);
                                key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                            }
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
                int count = 0;
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    count += sc.read(buffer);
                    System.out.println("count: " + count);
                    buffer.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
