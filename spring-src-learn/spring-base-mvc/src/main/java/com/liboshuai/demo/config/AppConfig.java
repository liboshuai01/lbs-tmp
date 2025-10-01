package com.liboshuai.demo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

/**
 * Spring的核心配置类，等同于applicationContext.xml。
 * 这个类负责配置非Web相关的Bean，如Service、Repository、Aspect等。
 *
 * @Configuration 声明这是一个Java配置类。
 * @EnableAspectJAutoProxy 启用AspectJ的自动代理支持，这是让AOP生效的关键。
 * @ComponentScan 配置组件扫描的规则。
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(
        basePackages = "com.liboshuai.demo", // 扫描的基础包路径
        // 排除扫描Web相关的Bean（@Controller, @RestController等），这些应由WebConfig来管理。
        // 这是为了避免同一个Bean被父子容器重复创建。
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class, Configuration.class})
        }
)
public class AppConfig {
    // 这里可以定义一些全局的Bean，例如数据源(DataSource)、事务管理器(TransactionManager)等。
    // 在本例中，因为Service和Aspect都用了@Service和@Component，所以通过组件扫描即可，无需手动@Bean定义。
}
