package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo01 {
    public static void main(String[] args) {
        // 获取系统类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        log.info("systemClassLoader: {}", systemClassLoader);

        // 获取其上层：扩展类加载器
        ClassLoader extClassLoader = systemClassLoader.getParent();
        log.info("extClassLoader: {}", extClassLoader);

        // 获取其上层：
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        log.info("bootstrapClassLoader: {}", bootstrapClassLoader);
    }
}
