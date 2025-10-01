package com.liboshuai.demo;

import com.liboshuai.demo.component.Component1;
import com.liboshuai.demo.event.UserRegisteredEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

import java.util.Locale;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // 来源自`MessageSource`接口，提供国际化（i18n）能力
        System.out.println(context.getMessage("hi", null, Locale.ENGLISH));
        System.out.println(context.getMessage("hi", null, Locale.JAPAN));
        System.out.println(context.getMessage("hi", null, Locale.CHINA));

        // 来源自`ResourcePatternResolver`接口，提供根据路径匹配模式加载资源的能力
        // 例如，`classpath*:`前缀可以加载所有jar包中符合该路径的资源文件
        Resource[] resources = context.getResources("classpath*:META-INF/spring.factories");
        for (Resource resource : resources) {
            System.out.println("resource: " + resource);
        }

        // 来源自`EnvironmentCapable`接口，提供获取应用运行环境（Environment）的能力
        // 通过`Environment`对象，可以访问应用的各种配置源，如配置文件、JVM系统属性、环境变量和命令行参数等
        ConfigurableEnvironment environment = context.getEnvironment();
        System.out.println("java_home: " + environment.getProperty("JAVA_HOME"));
        System.out.println("server.port: " + environment.getProperty("server.port"));

        // 来源自`ApplicationEventPublisher`接口，提供事件推送功能
//        context.publishEvent(new UserRegisteredEvent(context));
        context.getBean(Component1.class).register();
    }
}
