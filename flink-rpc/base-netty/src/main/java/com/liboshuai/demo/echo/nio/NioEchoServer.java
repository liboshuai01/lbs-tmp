package com.liboshuai.demo.echo.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO（非阻塞I/O）实现的回声服务器
 * 这个服务器会监听指定的端口，接收客户端连接，
 * 然后将客户端发送的任何消息原样返回给客户端。
 */
@Slf4j
public class NioEchoServer {

    public static void main(String[] args) {
        // Java 1.7 引入的 try-with-resources 语法，可以自动关闭实现了 AutoCloseable 接口的资源。
        // Selector 和 ServerSocketChannel 都需要在使用后关闭，这种写法可以确保它们在任何情况下（即使是发生异常）都能被正确关闭。
        try (Selector selector = Selector.open(); // 1. 创建 Selector（选择器）
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) { // 2. 创建 ServerSocketChannel

            // 3. 绑定服务器端口
            serverSocketChannel.bind(new InetSocketAddress(8080));
            // 4. 设置为非阻塞模式
            // 这是NIO的关键。在非阻塞模式下，accept() 方法会立即返回，如果没有新的连接，则返回null。
            serverSocketChannel.configureBlocking(false);
            // 5. 将 ServerSocketChannel 注册到 Selector
            // - selector: 我们创建的选择器实例。
            // - SelectionKey.OP_ACCEPT: 表示我们对“接受新连接”这个事件感兴趣。
            // 当有新的客户端尝试连接时，Selector 会通知我们。
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO回声服务器已启动，端口：8080");

            // 6. 进入主循环，持续监听事件
            while (true) {
                // selector.select() 是一个阻塞方法，它会一直等待，直到至少有一个已注册的通道准备好了我们感兴趣的事件。
                // 返回值是准备就绪的通道的数量。
                // 如果返回值是0，表示没有通道准备好，我们可以继续下一次循环。
                if (selector.select() == 0) {
                    continue;
                }

                // 7. 获取所有准备就绪的事件的 SelectionKey 集合
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                // 8. 使用迭代器遍历这个集合
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    // 将单个key的处理逻辑放在try-catch中，可以隔离问题。
                    // 即使处理某个客户端连接时发生错误，也不会导致整个服务器崩溃。
                    try {
                        // 9. 根据事件类型，分发到不同的处理方法
                        if (key.isAcceptable()) {
                            // 如果是“接受连接”事件
                            handleAccept(key, selector);
                        } else if (key.isReadable()) {
                            // 如果是“通道可读”事件（即客户端发送了数据）
                            handleReadAndWrite(key);
                        }
                    } catch (IOException e) {
                        System.err.println("处理客户端连接时出现IO异常：" + e.getMessage());
                        // 当发生IO异常时（例如客户端强制断开连接），我们需要做一些清理工作。
                        // a. 取消这个key，这样selector就不会再监听这个key上的事件了。
                        key.cancel();
                        try {
                            // b. 关闭与这个key关联的channel。
                            key.channel().close();
                        } catch (IOException ex) {
                            System.err.println("关闭异常channel时出错：" + ex.getMessage());
                        }
                    } finally {
                        // 10. *** 非常重要的一步 ***
                        // 在处理完一个key后，必须手动从selectedKeys集合中移除它。
                        // 因为Selector不会自己移除。如果不移除，下一次select()返回时，这个key还会被包含在内，
                        // 导致我们重复处理同一个事件，可能会引发逻辑错误或死循环。
                        keyIterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("服务器主循环出现严重异常", e);
        }
    }

    /**
     * 处理新的客户端连接
     * @param key 与ServerSocketChannel关联的SelectionKey
     * @param selector 服务器的主选择器
     * @throws IOException
     */
    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        // a. 从SelectionKey中获取关联的ServerSocketChannel
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        // b. 调用accept()方法接受客户端连接，并获取代表这个连接的SocketChannel
        // 因为我们知道这个事件已经发生了，所以在非阻塞模式下调用accept()会立即返回一个有效的SocketChannel
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            // c. 将新创建的SocketChannel也设置为非阻塞模式
            clientChannel.configureBlocking(false);
            // d. 将这个新的SocketChannel也注册到同一个Selector上，并监听OP_READ事件
            // 这样，当这个客户端发送数据时，我们就能在主循环中收到通知
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("接受到新的客户端连接：" + clientChannel.getRemoteAddress());
        }
    }

    /**
     * 处理客户端发送的数据，并回写数据
     * @param key 与客户端SocketChannel关联的SelectionKey
     * @throws IOException
     */
    private static void handleReadAndWrite(SelectionKey key) throws IOException {
        // a. 从SelectionKey中获取关联的客户端SocketChannel
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // b. 创建一个ByteBuffer用于读取数据。这是NIO中数据传输的核心容器。
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // c. 从channel中读取数据到buffer中。
        // 返回值bytesRead表示读取到的字节数。
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            // d. 读取到了数据
            // buffer.flip() 是一个关键操作。它将buffer从“写模式”切换到“读模式”。
            // 它会把limit设置为当前position，然后把position重置为0。这样我们就可以从头开始读取刚刚写入的数据。
            buffer.flip();
            // e. 使用UTF-8字符集将ByteBuffer中的字节解码为字符串
            String receivedMessage = StandardCharsets.UTF_8.decode(buffer).toString();
            System.out.println("接收到来自 " + clientChannel.getRemoteAddress() + " 的信息：" + receivedMessage.trim());

            // f. 实现“回声”功能：将接收到的数据原样写回给客户端
            // buffer.rewind() 将position重置为0，limit保持不变。这样我们可以重新读取buffer中的全部内容。
            buffer.rewind();
            // g. 将buffer中的数据写回到channel中
            clientChannel.write(buffer);
        } else if (bytesRead == -1) {
            // h. 如果read()方法返回-1，表示客户端已经正常关闭了连接（例如，客户端的socket.close()被调用）
            System.out.println("客户端 " + clientChannel.getRemoteAddress() + " 已断开连接");
            // i. 取消key，并关闭channel，释放资源
            key.cancel();
            clientChannel.close();
        }
        // 如果bytesRead为0，表示当前没有数据可读，我们什么也不做，等待下一次的读事件。
    }
}
