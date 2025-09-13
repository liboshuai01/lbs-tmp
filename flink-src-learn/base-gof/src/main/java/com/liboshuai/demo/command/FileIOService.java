package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 接收者（Receiver）1：负责执行文件IO相关的具体操作
 */
@Slf4j
public class FileIOService {
    public void saveFile(String fileName) {
      log.info("文件操作：正在将内容保存到文件 '{}'", fileName);
      // 模拟真实的文件保存耗时
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.info("保存文件被中断", e);
            Thread.currentThread().interrupt();
        }
        log.info("文件操作：文件 '{}' 保存成功", fileName);
    }
}
