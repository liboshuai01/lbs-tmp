package com.liboshuai.demo.base.spring;

import com.liboshuai.demo.base.spring.config.AppConfig;
import com.liboshuai.demo.base.spring.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class Application {
    public static void main(String[] args) {
        // --- 场景一：当前系统使用 MySQL ---
        System.out.println("================ 场景一：激活 MySQL 环境 ================");
        runAppWithProfile("mysql");

        System.out.println("\n");

        // --- 场景二：老板要求切换到 Oracle ---
        System.out.println("================ 场景二：激活 Oracle 环境 ================");
        runAppWithProfile("oracle");
    }
    private static void runAppWithProfile(String profile) {
        // 1. 创建 Spring 容器
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // 2. 获取环境对象，并设置激活的环境（Profile）
        ConfigurableEnvironment environment = context.getEnvironment();
        environment.setActiveProfiles(profile); // 动态设置当前环境

        // 3. 注册配置类并刷新容器
        context.register(AppConfig.class);
        context.refresh();

        // 4. 从容器中获取 UserService 的实例
        UserService userService = context.getBean(UserService.class);

        // 5. 调用业务方法
        userService.registerUser("张三");

        // 6. 关闭容器
        context.close();
    }
}
