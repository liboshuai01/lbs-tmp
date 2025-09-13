package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 接收者（Receiver）2：负责执行日志相关的具体操作
 */
@Slf4j
public class LoggingService {
    public void log(String level, String message) {
      log.info("日志记录: [{}] {}", level, message);
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            log.error("日志记录被中断", e);
            Thread.currentThread().interrupt();
        }
        log.info("日志记录：完成。");
    }
}
