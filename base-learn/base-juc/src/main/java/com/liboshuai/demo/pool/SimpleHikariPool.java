package com.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易数据库连接池实现
 * 模拟 HikariCP 的核心借还逻辑，练习 JUC 编程
 */
@Slf4j
public class SimpleHikariPool {

    // --- 配置参数 ---
    private final String url;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final long connectionTimeout; // 毫秒

    // --- JUC 核心组件 ---
    private final BlockingQueue<Connection> idleQueue;
    private final AtomicInteger totalConnectionCount = new AtomicInteger(0);
    private final Object lock = new Object();

    public SimpleHikariPool(String url, String username, String password, int maxPoolSize) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.connectionTimeout = 30000; // 30秒超时

        // 🌟【关键修改】将 true 改为 false (非公平模式)
        // 公平锁在高并发下会导致严重的吞吐量下降，甚至看似死锁。
        // 连接池通常优先追求获取性能，而非绝对的先来后到。
        this.idleQueue = new ArrayBlockingQueue<>(maxPoolSize, false);

        log.info("连接池初始化完成。最大连接数: {}", maxPoolSize);
    }

    /**
     * 获取连接 (核心方法)
     */
    public Connection getConnection() throws SQLException {
        long startTime = System.currentTimeMillis();

        // 1. 快速尝试：从空闲队列中获取（非阻塞）
        Connection conn = idleQueue.poll();
        if (conn != null) {
            if (isValid(conn)) {
                return createProxyConnection(conn); // 包装一下，确保之前的代理没干扰
            } else {
                closePhysicalConnection(conn);
                totalConnectionCount.decrementAndGet();
                // 递归重试（注意：递归深度过深可能会栈溢出，但在连接池场景通常没事）
                return getConnection();
            }
        }

        // 2. 扩容尝试：如果没有空闲连接，且未达上限，尝试创建
        if (totalConnectionCount.get() < maxPoolSize) {
            synchronized (lock) {
                // 双重检查
                if (totalConnectionCount.get() < maxPoolSize) {
                    Connection newConn = createPhysicalConnection();
                    totalConnectionCount.incrementAndGet();
                    log.debug("创建新连接，当前总数: {}", totalConnectionCount.get());
                    return createProxyConnection(newConn);
                }
            }
            // 💡 如果进入了 if 但没进 synchronized 内部（被别人抢先创建了），
            // 说明池子满了，此时应该立即去排队，或者再 poll 一次防止刚创建的立马被还回来了。
        }

        // 3. 阻塞等待：如果无法创建，则阻塞等待空闲连接
        try {
            long remaining = connectionTimeout - (System.currentTimeMillis() - startTime);
            if (remaining <= 0) {
                throw new SQLException("获取连接超时 (快速失败)");
            }

            // 阻塞等待
            conn = idleQueue.poll(remaining, TimeUnit.MILLISECONDS);

            if (conn == null) {
                throw new SQLException("获取连接超时！等待时间: " + connectionTimeout + "ms. 当前池总数: " + totalConnectionCount.get() + ", 队列大小: " + idleQueue.size());
            }

            if (isValid(conn)) {
                return createProxyConnection(conn);
            } else {
                closePhysicalConnection(conn);
                totalConnectionCount.decrementAndGet();
                return getConnection();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接线程被中断", e);
        }
    }

    /**
     * 归还连接到池中
     */
    private void recycle(Connection physicalConnection) {
        if (physicalConnection != null) {
            // 这里的 offer 必须成功，否则就会泄漏。
            // 在我们的逻辑里，队列容量 == maxPoolSize，且只有创建出来的才会被 put 进去，理论上不会满。
            boolean success = idleQueue.offer(physicalConnection);
            if (!success) {
                log.warn("【严重警告】连接归还失败，队列已满！销毁连接。");
                closePhysicalConnection(physicalConnection);
                totalConnectionCount.decrementAndGet();
            } else {
                // 日志量太大可以注释掉这行，只保留 debug
                // log.debug("连接已归还");
            }
        }
    }

    // --- 以下辅助方法保持不变 ---

    private Connection createPhysicalConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private void closePhysicalConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("关闭物理连接失败", e);
        }
    }

    private boolean isValid(Connection conn) {
        try {
            return conn != null && conn.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createProxyConnection(Connection realConn) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(realConn)
        );
    }

    private class ConnectionHandler implements InvocationHandler {
        private final Connection realConnection;

        public ConnectionHandler(Connection realConnection) {
            this.realConnection = realConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                SimpleHikariPool.this.recycle(realConnection);
                return null;
            }
            if ("isClosed".equals(method.getName())) {
                return false;
            }
            return method.invoke(realConnection, args);
        }
    }
}