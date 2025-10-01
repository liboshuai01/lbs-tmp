package com.liboshuai.demo;

import com.liboshuai.demo.config.AppConfig;
import com.liboshuai.demo.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        // 1. 使用 AnnotationConfigApplicationContext 从 Java 配置类加载容器
        // 不再使用 ClassPathXmlApplicationContext
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // 2. 从容器中获取Bean
        // 我们不再需要手动 new UserService()，而是直接从容器中获取
        UserService userService = context.getBean("userService", UserService.class);

        // 3. 调用方法
        userService.addUser("张三");
    }
}
