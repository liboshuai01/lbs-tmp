package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class LoggingService {
    public void log(String level, String message) {
        log.info("--- 开始写日志，等级：{}, 内容：{} ---", level, message);
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            log.error("写日志被中断", e);
            Thread.currentThread().interrupt();
        }
        log.info("--- 写日志完毕，等级：{}, 内容：{} ---", level, message);
    }
}
