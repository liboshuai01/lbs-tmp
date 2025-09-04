package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;
import sun.misc.Launcher;

import java.net.URL;

@Slf4j
public class Demo02 {
    public static void main(String[] args) {
        log.info(">>>>>>>>>> 启动类加载器 <<<<<<<<<");
        // 获取 BootstrapClassLoader 能够加载的 api 的路径
        URL[] bootstrapUrLs = Launcher.getBootstrapClassPath().getURLs();
        for (URL bootstrapUrL : bootstrapUrLs) {
            log.info("{}", bootstrapUrL);
        }

        log.info("");
        log.info(">>>>>>>>>> 扩展类加载器 <<<<<<<<<");
        String extDirs = System.getProperty("java.ext.dirs");
        for (String path : extDirs.split(";")) {
            log.info("{}", path);
        }
    }
}
