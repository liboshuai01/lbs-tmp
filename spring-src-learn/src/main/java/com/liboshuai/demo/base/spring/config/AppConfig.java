package com.liboshuai.demo.base.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration // 声明这是一个配置类
@ComponentScan("com.liboshuai.demo.base.spring") // 告诉 Spring 去扫描 "com.example.demo" 包下的所有组件
public class AppConfig {
}
