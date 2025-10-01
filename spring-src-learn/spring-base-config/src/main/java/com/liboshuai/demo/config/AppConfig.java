package com.liboshuai.demo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 这是一个 Spring 的配置类，它完全代替了 applicationContext.xml 文件。
 */
@Configuration  // 1. @Configuration: 声明这个类是一个 Spring 配置类
@ComponentScan(basePackages = "com.liboshuai.demo") // @ComponentScan: 等同于 XML 中的 context:component-scan，开启组件扫描。
@EnableAspectJAutoProxy // 3. @EnableAspectJAutoProxy：等同于 XML 中的 <aop:aspectj-autoproxy/>，开启AOP注解支持。
public class AppConfig {
    // 这个类可以是空的，因为我们所有的Bean都通过@ComponentScan来自动注册。
    // 如果需要手动定义一些Bean（比如来自第三方库的类），可以在这里添加@Bean方法。
}
