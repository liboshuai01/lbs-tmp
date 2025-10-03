package com.liboshuai.demo.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC的配置类，等同于spring-mvc.xml。
 * 这个类专门用于配置Web相关的Bean，如Controller、视图解析器、拦截器等。
 *
 * @Configuration 声明这是一个Java配置类。
 * @EnableWebMvc 启用Spring MVC的核心功能，例如支持@Controller、@RequestMapping等注解。
 * @ComponentScan 只扫描Controller，以避免与AppConfig的扫描范围冲突。
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.liboshuai.demo.controller")
public class WebConfig implements WebMvcConfigurer {
    // WebMvcConfigurer接口提供了很多回调方法来定制Spring MVC的配置。
    // 例如，配置视图解析器、静态资源处理器、拦截器等。
    // 因为我们是RESTful服务，直接返回JSON，所以这里不需要配置视图解析器(ViewResolver)。
}
