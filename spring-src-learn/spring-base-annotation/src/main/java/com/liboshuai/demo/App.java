package com.liboshuai.demo;

import com.liboshuai.demo.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        // 1. 初始化Spring容器，加载配置文件
        // ClassPathXmlApplicationContext会从类路径下查找配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        // 2. 从容器中获取Bean
        // 我们不再需要手动 new UserService()，而是直接从容器中获取
        UserService userService = context.getBean("userService", UserService.class);

        // 3. 调用方法
        userService.addUser("张三");
    }
}
