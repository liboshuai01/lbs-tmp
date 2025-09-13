package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class FileIOService {
    public void saveFile(String fileName) {
        log.info("--- 开始保存文件，名称： {} ---", fileName);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            log.error("保存文件被中断", e);
            Thread.currentThread().interrupt();
        }
        log.info("--- 保存文件完毕，名称：{} ---", fileName);
    }
}
