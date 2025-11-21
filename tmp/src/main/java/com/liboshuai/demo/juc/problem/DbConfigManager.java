package com.liboshuai.demo.juc.problem;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DbConfigManager {

    private String host = "127.0.0.1";
    private int port = 3306;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    /**
     * 获取完整的连接地址 (高并发读取)
     */
    public String getUrl() {
        readLock.lock();
        try {
            return host + ":" + port;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 更新配置 (低频写入)
     * @param newHost 新的IP
     * @param newPort 新的端口
     */
    public void update(String newHost, int newPort) {
        readLock.lock();
        try {
            if (host.equals(newHost) && port == newPort) {
                return;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (host.equals(newHost) && port == newPort) {
                return;
            }
            System.out.println("开始更新配置...");

            // 步骤A：更新 IP
            this.host = newHost;

            // 模拟更新过程中的微小延迟 (比如日志打印、通知其他组件等)
            try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException e) {}

            // 步骤B：更新 端口
            this.port = newPort;

            System.out.println("配置更新完成: " + newHost + ":" + newPort);
        } finally {
            writeLock.unlock();
        }
    }
}
