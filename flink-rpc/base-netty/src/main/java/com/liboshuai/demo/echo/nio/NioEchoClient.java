package com.liboshuai.demo.echo.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * NIO 实现的回声客户端
 * 这个客户端会连接到服务器，从控制台读取用户输入，
 * 将输入发送到服务器，然后接收服务器返回的数据并打印出来。
 */
@Slf4j
public class NioEchoClient {
    public static void main(String[] args) {
        // 同样使用 try-with-resources 来确保 SocketChannel 和 Scanner 在使用完毕后能被自动关闭。
        try (SocketChannel socketChannel = SocketChannel.open(); // 1. 创建客户端 SocketChannel
             Scanner scanner = new Scanner(System.in)) { // 2. 创建 Scanner 用于读取控制台输入

            // 3. 连接到服务器
            // 与服务器端的 bind 不同，客户端使用 connect
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

            // 4. 等待连接完成
            // 在非阻塞模式下，connect() 可能不会立即完成连接。
            // finishConnect() 方法可以检查连接过程是否已经完成。
            // 在阻塞模式下（默认），connect() 会阻塞直到连接成功或失败，所以这个循环不是必须的，但这是一个好的实践。
            while (!socketChannel.finishConnect()) {
                System.out.println("正在连接服务器...");
                // 可以在这里做一些其他事情，或者短暂休眠，避免空转消耗CPU
                // Thread.sleep(100);
            }

            System.out.println("已连接至服务器，主机：127.0.0.1， 端口：8080");
            System.out.println("请输入文本（输入exit退出）：");

            // 5. 进入主循环，处理用户输入和网络通信
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // 检查用户是否输入 "exit" 来退出循环
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }

                // 6. 发送数据到服务器
                // a. 将用户输入的字符串编码为字节，并包装到一个 ByteBuffer 中。
                // ByteBuffer.wrap() 会创建一个刚好能容纳这些字节的缓冲区。
                ByteBuffer writeBuffer = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
                // b. 调用 write 方法将缓冲区中的数据发送出去。
                socketChannel.write(writeBuffer);

                // 7. 接收服务器返回的回显数据
                // a. 创建一个固定大小的 ByteBuffer 来接收数据。
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // b. 从 channel 读取数据到缓冲区。这是一个阻塞操作（在默认的阻塞模式下）。
                int bytesRead = socketChannel.read(readBuffer);
                if (bytesRead > 0) {
                    // c. 读取到了数据，将缓冲区从“写模式”切换到“读模式”。
                    readBuffer.flip();
                    // d. 将缓冲区中的字节解码为字符串。
                    String receivedMessage = StandardCharsets.UTF_8.decode(readBuffer).toString().trim();
                    System.out.println("接收到服务端的响应数据：" + receivedMessage);
                    // 提示用户继续输入
                    System.out.println("请输入文本（输入exit退出）：");
                }
            }

        } catch (IOException e) {
            log.error("客户端出现IO异常",e);
        } finally {
            // 当 try-with-resources 块结束时（无论是正常结束还是因为异常），
            // socketChannel 和 scanner 的 close() 方法会被自动调用。
            // 这个 finally 块会在资源关闭后执行。
            System.out.println("客户端已关闭。");
        }
    }
}
