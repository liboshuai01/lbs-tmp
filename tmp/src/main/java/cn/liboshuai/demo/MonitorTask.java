package cn.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MonitorTask {

    private static final Logger log = LoggerFactory.getLogger(MonitorTask.class);

    private final Thread monitorThread;

    public MonitorTask() {
        monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                log.info("正在进行监控采集工作....");
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    log.info("接收到停止消息, 准备关闭监控线程");
                    Thread.currentThread().interrupt();
                }
            }
            clearResource();
        }, "监控线程");
    }

    public static void main(String[] args) throws InterruptedException {
        MonitorTask monitorTask = new MonitorTask();
        monitorTask.start();
        TimeUnit.SECONDS.sleep(2);
        monitorTask.stop();
    }

    public void start() {
        monitorThread.start();
    }

    public void stop() {
        log.info("发送停止信息, 准备停止监控线程....");
        monitorThread.interrupt();
    }

    public void clearResource() {
        log.info("清理关闭监控线程之前的一些资源");
    }
}
