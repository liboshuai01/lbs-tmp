package com.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 简易数据库连接池
 * * 核心设计：
 * 1. 初始化时创建固定数量的连接放入 MyBlockingQueue。
 * 2. borrow: 相当于消费者，从队列 take/poll。
 * 3. return: 相当于生产者，向队列 put/offer。
 */
@Slf4j
public class MiniConnectionPool {

    // 核心仓库：复用你之前手写的阻塞队列
    private final MyBlockingQueue<MockConnection> pool;

    // 配置大小
    private final int poolSize;

    // 开关状态
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public MiniConnectionPool(int poolSize) {
        this.poolSize = poolSize;
        // 初始化队列，容量即为池子大小
        this.pool = new MyBlockingQueue<>(poolSize);
        initConnections();
    }

    /**
     * 初始化：一次性把连接池填满
     * (真实场景中可能会用"懒加载"，即有人借的时候再创建)
     */
    private void initConnections() {
        log.info("⚙️ 正在初始化连接池，准备创建 {} 个连接...", poolSize);
        for (int i = 1; i <= poolSize; i++) {
            MockConnection conn = new MockConnection("Conn-" + i);
            try {
                // 放入队列，理论上初始化时队列是空的，绝对能放进去
                pool.put(conn);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("✅ 连接池初始化完成，当前可用连接数: {}", pool.size());
    }

    /**
     * 【核心方法】借出连接
     * @param timeout 超时时间
     * @return 连接对象，如果超时未获取到则返回 null
     */
    public MockConnection borrow(long timeout, TimeUnit unit) {
        if (isShutdown.get()) {
            throw new IllegalStateException("连接池已关闭");
        }

        long start = System.currentTimeMillis();
        try {
            // 尝试从队列获取，如果队列空了，会阻塞在这里直到超时
            MockConnection conn = pool.poll(timeout, unit);

            if (conn == null) {
                log.warn("⚠️ 获取连接超时 (等待了 {} ms)", System.currentTimeMillis() - start);
                return null;
            }

            // (进阶点) 这里可以加一个 conn.isValid() 检查，如果失效了就销毁重造

            log.debug("📤 借出连接: {}, 耗时: {}ms", conn.getName(), System.currentTimeMillis() - start);
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 【核心方法】归还连接
     * @param conn 要归还的连接
     */
    public void returnConnection(MockConnection conn) {
        if (conn == null) return;

        if (isShutdown.get()) {
            log.info("连接池已关闭，销毁连接: {}", conn.getName());
            return;
        }

        try {
            // 将连接放回队列，唤醒正在 borrow 等待的线程
            pool.put(conn);
            log.debug("📥 归还连接: {}, 当前可用: {}", conn.getName(), pool.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭连接池
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            log.info("🚫 连接池正在关闭...");
            // 真实场景这里需要遍历队列，把所有连接 close() 掉
        }
    }

    // 用于监控
    public int getIdleCount() {
        return pool.size();
    }
}