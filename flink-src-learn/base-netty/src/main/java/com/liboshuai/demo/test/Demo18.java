package com.liboshuai.demo.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Demo18 {
    static class Client {
        public static void main(String[] args) throws IOException {
            // 创建一个Socket并连接到指定服务器的8080端口
            Socket socket = new Socket("127.0.0.1", 8080);
            System.out.println("成功连接到服务器...");

            // 获取输出流，用于向服务器发送数据
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            System.out.println("请输入要发送给服务器的消息 (输入 'exit' 退出):");

            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                // 将字符串转换为字节并发送
                outputStream.write(message.getBytes());
                // 添加换行符作为消息分隔
                outputStream.write("\n".getBytes());
                outputStream.flush();
            }

            // 关闭资源
            scanner.close();
            outputStream.close();
            socket.close();
            System.out.println("客户端已关闭。");
        }
    }

    static class BioServer {
        public static void main(String[] args) throws IOException {
            // 1. 创建一个ServerSocket，监听8080端口
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("BIO 服务器已启动，监听端口: " + serverSocket.getLocalPort());

            while (true) {
                // 2. 阻塞点1：等待客户端连接
                // 如果没有客户端连接，线程会一直阻塞在这里
                System.out.println("等待客户端连接...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端已连接: " + clientSocket.getRemoteSocketAddress());

                // 3. 为每个客户端连接创建一个新的线程来处理
                // 这是BIO模型的关键：用一个独立的线程隔离每个连接的阻塞操作
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String line;
                        // 4. 阻塞点2：等待客户端发送数据
                        // 如果客户端不发送数据，线程会一直阻塞在 readLine()
                        while ((line = reader.readLine()) != null) {
                            System.out.println("收到来自 " + clientSocket.getRemoteSocketAddress() + " 的消息: " + line);
                        }
                    } catch (IOException e) {
                        System.out.println("客户端 " + clientSocket.getRemoteSocketAddress() + " 连接异常: " + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close();
                            System.out.println("客户端 " + clientSocket.getRemoteSocketAddress() + " 已断开。");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    static class NioPollingServer {
        public static void main(String[] args) throws IOException {
            // 1. 创建一个ServerSocketChannel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            // 2. **关键：设置为非阻塞模式**
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(8080));
            System.out.println("NIO (轮询) 服务器已启动，监听端口: 8080");

            // 用一个列表存储所有连接的客户端Channel
            List<SocketChannel> clientChannels = new ArrayList<>();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                // 3. **非阻塞**：尝试接受新的连接
                // 这个调用会立即返回，如果没有新连接，则返回 null
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    // 4. **关键：新接入的连接也必须设置为非阻塞**
                    clientChannel.configureBlocking(false);
                    clientChannels.add(clientChannel);
                    System.out.println("客户端已连接: " + clientChannel.getRemoteAddress());
                }

                // 5. **核心：遍历所有已连接的客户端，轮询是否有数据可读**
                // 即使只有一个连接，这个循环也需要不断执行，消耗CPU
                for (int i = 0; i < clientChannels.size(); i++) {
                    SocketChannel channel = clientChannels.get(i);
                    try {
                        // 6. **非阻塞**：尝试读取数据
                        // 这个调用会立即返回，如果没数据，则返回0
                        int bytesRead = channel.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip(); // 切换到读模式
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String message = new String(bytes).trim();
                            System.out.println("收到来自 " + channel.getRemoteAddress() + " 的消息: " + message);
                            buffer.clear(); // 为下次写入做准备
                        } else if (bytesRead == -1) {
                            // 客户端断开连接
                            System.out.println("客户端 " + channel.getRemoteAddress() + " 已断开。");
                            channel.close();
                            clientChannels.remove(i);
                            i--; // 因为删除了元素，索引需要减1
                        }
                        // bytesRead == 0 的情况，表示没有数据，直接忽略，继续轮询
                    } catch (IOException e) {
                        // 发生异常，同样移除
                        System.out.println("客户端 " + channel.getRemoteAddress() + " 连接异常: " + e.getMessage());
                        channel.close();
                        clientChannels.remove(i);
                        i--;
                    }
                }

                // 为了防止CPU空转100%，可以加一个短暂的休眠，但这并非最佳实践
                // try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
        }
    }

    static class NioSelectorServer  {
        public static void main(String[] args) throws IOException {
            // 1. 创建 Selector
            Selector selector = Selector.open();

            // 2. 创建 ServerSocketChannel，并设置为非阻塞
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(8080));

            // 3. **核心：将 Channel 注册到 Selector，并监听 OP_ACCEPT 事件**
            // 这个注册告诉Selector：“我对这个Channel的'接受连接'事件感兴趣”
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO (Selector) 服务器已启动，监听端口: 8080");
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                // 4. **阻塞点：调用 select()**
                // 这个方法会阻塞，直到有一个或多个已注册的事件发生（例如：新连接、数据可读）
                // 如果没有事件，它会一直等待，此时线程是休眠的，不会消耗CPU
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue; // 如果没有事件，继续循环
                }

                // 5. 获取所有已就绪事件的 SelectionKey
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    // 6. 根据事件类型进行处理
                    if (key.isAcceptable()) {
                        // 是一个新的连接事件
                        ServerSocketChannel svrChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = svrChannel.accept();
                        clientChannel.configureBlocking(false);

                        // **将新的客户端连接也注册到Selector上，并监听 OP_READ 事件**
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("客户端已连接: " + clientChannel.getRemoteAddress());

                    } else if (key.isReadable()) {
                        // 是一个数据可读事件
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        try {
                            int bytesRead = clientChannel.read(buffer);
                            if (bytesRead > 0) {
                                buffer.flip();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                String message = new String(bytes).trim();
                                System.out.println("收到来自 " + clientChannel.getRemoteAddress() + " 的消息: " + message);
                                buffer.clear();
                            } else if (bytesRead == -1) {
                                // 客户端断开连接
                                System.out.println("客户端 " + clientChannel.getRemoteAddress() + " 已断开。");
                                key.cancel(); // 取消注册
                                clientChannel.close();
                            }
                        } catch (IOException e) {
                            System.out.println("客户端 " + clientChannel.getRemoteAddress() + " 连接异常，断开。");
                            key.cancel();
                            clientChannel.close();
                        }
                    }

                    // 7. **关键：处理完事件后，必须手动移除，否则会重复处理**
                    keyIterator.remove();
                }
            }
        }
    }
}
