package com.liboshuai.demo.juc.chm;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;

public class RpcConnectionManager {

    // 模拟一个连接对象
    public static class RpcConnection implements Closeable {
        private final String name;
        public RpcConnection(String name) { this.name = name; }

        @Override
        public void close() {
            System.out.println("Closing redundant connection: " + name);
        }
        @Override
        public String toString() { return "Conn[" + name + "]"; }
    }

    private final ConcurrentHashMap<String, RpcConnection> connections = new ConcurrentHashMap<>();

    /**
     * 获取或建立连接 (高性能模式)
     * * @param targetAddress 目标地址
     * @return 有效的连接对象
     */
    public RpcConnection getOrEstablishConnection(String targetAddress) {
        // 1. 快速检查：如果有了直接返回 (Double-Check 的第一层)
        RpcConnection existing = connections.get(targetAddress);
        if (existing != null) {
            return existing;
        }

        // 2. 模拟耗时的连接创建过程 (在锁外进行，避免阻塞 Map)
        RpcConnection newConn = new RpcConnection(targetAddress + "-" + System.nanoTime());

        // TODO: 请在这里编写代码
        // 核心逻辑：
        // 尝试将 newConn 放入 Map。
        // 情况 A: Map 中之前不存在 -> 放入成功，返回 newConn
        // 情况 B: Map 中已经存在了 (被别的线程抢先了) -> 放入失败，返回那个已存在的连接，并且 **记得关闭(close)** 我们刚创建的 newConn
        RpcConnection rpcConnection = connections.putIfAbsent(targetAddress, newConn);
        if (rpcConnection != null && newConn != (rpcConnection)) {
            newConn.close();
        }
        return rpcConnection == null ? newConn : rpcConnection; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        RpcConnectionManager manager = new RpcConnectionManager();
        String target = "192.168.0.1";

        // 模拟两个线程同时尝试建立连接
        Runnable task = () -> {
            RpcConnection conn = manager.getOrEstablishConnection(target);
            System.out.println(Thread.currentThread().getName() + " got: " + conn);
        };

        new Thread(task, "Thread-A").start();
        new Thread(task, "Thread-B").start();
    }
}
