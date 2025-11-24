package cn.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final AtomicBoolean starting = new AtomicBoolean(false);

    public void start() {
        if (!starting.compareAndSet(false, true)) {
            log.info("服务已经启动了, 无需重复启动, 跳过此次操作");
            return;
        }
        log.info("开始启动服务...");
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            log.info("启动服务中被中断了");
            Thread.currentThread().interrupt();
        }
        log.info("服务启动成功...");
    }

    public static void main(String[] args) {
        MonitorService monitorService = new MonitorService();
        new Thread(monitorService::start, "t1").start();
        new Thread(monitorService::start, "t2").start();
        new Thread(monitorService::start, "t3").start();
    }
}
