package com.liboshuai.demo.pool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟一个数据库连接对象
 * 它的创建成本很高，所以我们需要"池化"它，而不是每次都 new
 */
@Slf4j
public class MockConnection {

    @Getter
    private final String name;
    private final long createTime;

    // 记录这个连接一共被使用了多少次 (用来观察复用率)
    private final AtomicInteger useCount = new AtomicInteger(0);

    public MockConnection(String name) {
        this.name = name;
        this.createTime = System.currentTimeMillis();
        // 模拟连接建立的耗时 (比如 TCP 三次握手)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 模拟执行 SQL 语句
     */
    public void executeQuery(String sql) {
        log.info("📡 [{}] 正在执行SQL: \"{}\"", name, sql);
        try {
            // 模拟数据库查询耗时 (10ms - 100ms)
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        useCount.incrementAndGet();
    }

    /**
     * 检查连接是否健康 (心跳检测)
     */
    public boolean isValid() {
        // 简单模拟：连接存活时间超过 10分钟 就算超时断开
        return System.currentTimeMillis() - createTime < 10 * 60 * 1000;
    }

    @Override
    public String toString() {
        return "MockConnection{name='" + name + "', used=" + useCount.get() + "次}";
    }
}