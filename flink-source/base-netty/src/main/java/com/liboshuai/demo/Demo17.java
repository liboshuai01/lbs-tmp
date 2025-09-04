package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Demo17 {
    static class Server {

        public static void main(String[] args) {
            try (ServerSocketChannel ssc = ServerSocketChannel.open();
                 Selector boss = Selector.open()) {
                ssc.bind(new InetSocketAddress("127.0.0.1", 8080));
                ssc.configureBlocking(false);
                ssc.register(boss, SelectionKey.OP_ACCEPT);
                int cpuCoreCount = Runtime.getRuntime().availableProcessors();
                Worker[] workers = new Worker[cpuCoreCount];
                for (int i = 0; i < cpuCoreCount; i++) {
                    workers[i] = new Worker("worker-" + i);
                }
                AtomicInteger count = new AtomicInteger(0);
                while (true) {
                    log.info("等待连接建立...");
                    boss.select();
                    Iterator<SelectionKey> bossIterator = boss.selectedKeys().iterator();
                    while (bossIterator.hasNext()) {
                        SelectionKey bossKey = bossIterator.next();
                        bossIterator.remove();
                        if (bossKey.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) bossKey.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            log.info("成功建立连接，客户端地址为：[{}]", socketChannel.getRemoteAddress());
                            socketChannel.configureBlocking(false);
                            workers[count.getAndIncrement() % workers.length].register(socketChannel);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        static class Worker implements Runnable{
            private final String name;
            private Selector selector;
            private final ConcurrentLinkedDeque<Runnable> deque = new ConcurrentLinkedDeque<>();
            private final AtomicBoolean start = new AtomicBoolean(false);

            public Worker(String name) {
                this.name = name;
            }

            public void register(SocketChannel sc) {
                if (start.compareAndSet(false, true)) {
                    try {
                        selector = Selector.open();
                        new Thread(this, name).start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                deque.addLast(() -> {
                    try {
                        sc.register(selector, SelectionKey.OP_READ);
                    } catch (ClosedChannelException e) {
                        throw new RuntimeException(e);
                    }
                });
                selector.wakeup();
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        selector.select();
                        Runnable task = deque.pollFirst();
                        if (task != null) {
                            task.run();
                        }
                        Iterator<SelectionKey> workerIterator = selector.selectedKeys().iterator();
                        while (workerIterator.hasNext()) {
                            SelectionKey workerKey = workerIterator.next();
                            workerIterator.remove();
                            if (workerKey.isReadable()) {
                                SocketChannel socketChannel = (SocketChannel) workerKey.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(256);
                                int length = 0;
                                try {
                                    length = socketChannel.read(buffer);
                                } catch (IOException e) {
                                    log.info("客户端异常关闭连接");
                                    workerKey.cancel();
                                }
                                if (length == -1) {
                                    log.info("客户端正常关闭连接");
                                    workerKey.cancel();
                                }
                                buffer.flip();
                                log.info("接收到的客户端[{}]的数据：{}", socketChannel.getRemoteAddress(), StandardCharsets.UTF_8.decode(buffer));
                                buffer.clear();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }

    static class Client {
        public static void main(String[] args) {
            try (SocketChannel sc = SocketChannel.open()) {
                sc.connect(new InetSocketAddress("127.0.0.1", 8080));
                int count = 0;
                while (true) {
                    sc.write(StandardCharsets.UTF_8.encode(count++ + ""));
                    TimeUnit.SECONDS.sleep(3);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
