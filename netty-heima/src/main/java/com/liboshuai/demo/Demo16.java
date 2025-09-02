package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

@Slf4j
public class Demo16 {
    static class Server {
        public static void main(String[] args) {
            try (Selector selector = Selector.open();
                 ServerSocketChannel ssc = ServerSocketChannel.open()) {
                ssc.configureBlocking(false);
                ssc.bind(new InetSocketAddress(8080));
                ssc.register(selector, SelectionKey.OP_ACCEPT);
                while (true) {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel sc = serverSocketChannel.accept();
                            sc.configureBlocking(false);
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            sc.register(selector, SelectionKey.OP_READ, buffer);
                        } else if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            int length = 0;
                            try {
                                length = sc.read(buffer);
                            } catch (IOException e) {
                                log.error("客户端异常关闭连接", e);
                                key.cancel();
                            }
                            if (length == -1) {
                                System.out.println("客户端正常关闭连接......");
                                key.cancel();
                            } else {
                                buffer.flip();
                                String data = StandardCharsets.UTF_8.decode(buffer).toString();
                                System.out.println("接收到的客户端信息：" + data);
                                buffer.rewind();
                                length = sc.write(buffer);
                                if (buffer.hasRemaining()) {
                                    key.interestOps(key.interestOps() + SelectionKey.OP_WRITE);
                                    key.attach(buffer);
                                }
                            }
                        } else if (key.isWritable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            int length = sc.write(buffer);
                            if (!buffer.hasRemaining()) {
                                key.attach(null);
                                key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                            }
                        } else {
                            log.warn("其他未支持事件类型......");
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
            try (SocketChannel sc = SocketChannel.open();
                 Scanner scanner = new Scanner(System.in)) {
                sc.connect(new InetSocketAddress("127.0.0.1", 8080));
                new Thread(() -> {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (true) {
                            buffer.clear();
                            int len = sc.read(buffer);
                            if (len > 0) {
                                buffer.flip();
                                String response = StandardCharsets.UTF_8.decode(buffer).toString();
                                System.out.println("收到服务器响应: " + response);
                            } else if (len == -1) {
                                System.out.println("服务器已断开连接。");
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },"reader-thread").start();
                System.out.println("请输入文本（exit退出）");
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(line)) {
                        break;
                    }
                    sc.write(StandardCharsets.UTF_8.encode(line));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
