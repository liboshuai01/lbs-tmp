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

        // 获取其上层：获取不到引导类加载器
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        log.info("bootstrapClassLoader: {}", bootstrapClassLoader);

        // 对于用户自定义类来说：默认使用系统类加载器进行加载
        ClassLoader demo01ClassLoader = Demo01.class.getClassLoader();
        log.info("demo01ClassLoader: {}", demo01ClassLoader);

        // String类使用引导类加载器进行加载（Java的核心类库都是使用引导类加载器进行加载的）
        ClassLoader stringClassLoader = String.class.getClassLoader();
        log.info("stringClassLoader: {}", stringClassLoader);
    }
}
