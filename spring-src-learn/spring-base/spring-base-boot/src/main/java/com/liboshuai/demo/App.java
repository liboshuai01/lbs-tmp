package com.liboshuai.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot的主启动类。
 *
 * @SpringBootApplication 是一个复合注解，它包含了三个核心注解：
 * 1. @SpringBootConfiguration: 继承自@Configuration，表明这个类是Spring配置类。
 * 2. @EnableAutoConfiguration: 开启Spring Boot的自动化配置。
 * Spring Boot会根据类路径下的依赖（例如，看到spring-boot-starter-web），
 * 自动配置相关的Bean（例如，自动配置Tomcat服务器、DispatcherServlet、Jackson等）。
 * 这就是为什么我们不再需要WebConfig和AppConfig。
 * 3. @ComponentScan: 自动扫描该类所在的包及其所有子包下的组件（@Component, @Service, @RestController等）。
 * 这就是为什么我们的Controller, Service, Aspect能被自动发现。
 */
@SpringBootApplication
public class App 
{
    /**
     * Java应用程序的主入口方法。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // SpringApplication.run() 负责启动整个应用程序。
        // 它会创建一个Spring应用上下文，执行自动化配置，并启动内嵌的Web服务器。
        SpringApplication.run(App.class, args);
    }
}
