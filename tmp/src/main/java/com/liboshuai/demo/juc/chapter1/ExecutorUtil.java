package com.liboshuai.demo.juc.chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtil {

    private static final Logger log = LoggerFactory.getLogger(ExecutorUtil.class);

    public static void close(ExecutorService executorService, long timeout, TimeUnit unit) {
        log.info("正在关闭线程池");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, unit)) {
                log.error("任务在 {} {}内未能停止, 尝试强制关闭...", timeout, unit.name());
                executorService.shutdownNow();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("线程池未能终止");
                }
            }
        } catch (InterruptedException e) {
            log.error("主线程在等待时被终端, 强制关闭线程池");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
