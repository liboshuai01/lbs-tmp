package com.liboshuai.demo.echo.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
public class NioEchoClient {
    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            // 连接到服务器
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
            // 等待连接完成
            while (!socketChannel.finishConnect()) {
                System.out.println("正在连接服务器...");
            }

            System.out.println("已连接至服务器，主机：127.0.0.1， 端口：8080");
            System.out.println("请输入文本（输入exit退出）：");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }

                // 发送数据
                ByteBuffer writeBuffer = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(writeBuffer);

                // 接收回显数据
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int bytesRead = socketChannel.read(readBuffer);
                if (bytesRead > 0) {
                    readBuffer.flip();
                    String receivedMessage = StandardCharsets.UTF_8.decode(readBuffer).toString().trim();
                    System.out.println("接收到服务端的响应数据：" + receivedMessage);
                    System.out.println("请输入文本（输入exit退出）：");
                }
            }

        } catch (IOException e) {
            log.error("",e);
        } finally {
            System.out.println("客户端已关闭。");
        }
    }
}