package com.liboshuai.demo.pool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SimpleHikariPoolTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleHikariPoolTest.class);

    // H2 内存数据库连接串
    // DB_CLOSE_DELAY=-1 保证虚拟机不退出，数据不丢失
    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private SimpleHikariPool pool;

    @Before
    public void setUp() throws Exception {
        // 加载 H2 驱动
        Class.forName("org.h2.Driver");
    }

    @After
    public void tearDown() {
        // 每个测试结束后清理，实际项目中可能需要给连接池加一个 shutdown 方法
        pool = null;
    }

    /**
     * 测试 1: 基础的获取与归还
     */
    @Test
    public void testBasicGetAndClose() throws SQLException {
        pool = new SimpleHikariPool(H2_URL, USER, PASSWORD, 2);

        // 1. 获取连接
        Connection conn1 = pool.getConnection();
        assertNotNull("连接不应为空", conn1);
        assertTrue("连接应该是有效的", conn1.isValid(1));

        // 2. 这里的 close 应该是“归还”而不是真正关闭
        conn1.close();
        assertTrue("调用close后，物理连接不应真正关闭(代理逻辑)", !conn1.isClosed());

        // 3. 再次获取，应该能获取到（复用或新建）
        Connection conn2 = pool.getConnection();
        assertNotNull(conn2);
        conn2.close();
    }

    /**
     * 测试 2: 验证连接池的大小限制 (Max Size)
     * 场景：池大小为 1，尝试获取 2 个连接，第 2 个应该阻塞直到超时。
     */
    @Test(expected = SQLException.class)
    public void testPoolExhaustionTimeout() throws Exception {
        pool = new SimpleHikariPool(H2_URL, USER, PASSWORD, 1);

        // 为了测试不跑太久，利用反射将 connectionTimeout 改为 1秒
        setPoolTimeout(pool, 1000L);

        // 1. 拿走唯一的一个连接，不归还
        Connection conn1 = pool.getConnection();
        assertNotNull(conn1);

        log.info("已拿走所有连接，准备尝试获取下一个（预计抛出超时异常）...");

        // 2. 尝试拿第二个，应该阻塞 1s 后抛出 SQLException
        pool.getConnection();
    }

    /**
     * 测试 3: 验证连接复用
     * 场景：池大小 1。借 -> 还 -> 借。第二次借到的应该是同一个物理对象（或者至少能借到）。
     */
    @Test
    public void testConnectionReuse() throws SQLException {
        pool = new SimpleHikariPool(H2_URL, USER, PASSWORD, 1);

        Connection conn1 = pool.getConnection();
        // 获取被代理对象的 toString 或者 hashcode 比较困难，
        // 这里简单验证归还后能再次立刻获取，说明归还逻辑生效。
        conn1.close();

        Connection conn2 = pool.getConnection();
        assertNotNull("归还后应能再次获取连接", conn2);
        conn2.close();
    }

    /**
     * 测试 4: JUC 高并发压力测试
     * 场景：池大小 5，启动 50 个线程，每个线程借用连接 10ms 后归还。
     * 验证：所有线程都能最终成功完成，没有死锁或报错。
     */
    @Test
    public void testConcurrencyStress() throws InterruptedException {
        int poolSize = 5;
        int threadCount = 50; // 模拟 50 个并发请求
        pool = new SimpleHikariPool(H2_URL, USER, PASSWORD, poolSize);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 用于收集异常信息
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 模拟业务：获取连接 -> 模拟耗时 -> 归还
                    Connection conn = pool.getConnection();
                    // 简单的业务操作
                    conn.isValid(1);
                    // 模拟持有连接一小段时间
                    Thread.sleep(10);
                    conn.close(); // 归还

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                    log.error("线程执行异常", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        executor.shutdown();

        long cost = System.currentTimeMillis() - start;
        log.info("并发测试完成。耗时: {}ms, 成功: {}, 失败: {}", cost, successCount.get(), failCount.get());

        if (!exceptions.isEmpty()) {
            failCount.get();
            // 打印第一个异常供调试
            throw new RuntimeException("存在失败的线程", exceptions.get(0));
        }

        assertEquals("所有线程都应成功获取并归还连接", threadCount, successCount.get());
    }

    /**
     * 辅助方法：利用反射修改私有的 connectionTimeout 字段，避免测试等待太久
     */
    private void setPoolTimeout(SimpleHikariPool targetPool, long timeoutMs) throws Exception {
        Field field = SimpleHikariPool.class.getDeclaredField("connectionTimeout");
        field.setAccessible(true);
        field.set(targetPool, timeoutMs);
    }
}