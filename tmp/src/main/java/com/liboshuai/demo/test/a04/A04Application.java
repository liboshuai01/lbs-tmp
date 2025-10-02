package com.liboshuai.demo.test.a04;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Bean 后处理器的作用
 */
public class A04Application {

    private static final Logger log = LoggerFactory.getLogger(A04Application.class);

    public static void main(String[] args) {
        // GenericApplicationContext 是一个【干净】的容器
        GenericApplicationContext context = new GenericApplicationContext();

        // 用原始方法注册三个 bean
        context.registerBean("bean1", Bean1.class);
        context.registerBean("bean2", Bean2.class);
        context.registerBean("bean3", Bean3.class);
        context.registerBean("bean4", Bean4.class);

        context.getDefaultListableBeanFactory().setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class); // @Autowired @Value 功能
        context.registerBean(CommonAnnotationBeanPostProcessor.class); // 提供 @Resource @PostConstruct @PreDestroy 功能
        ConfigurationPropertiesBindingPostProcessor.register(context.getDefaultListableBeanFactory()); // 提供 @ConfigurationProperties 能力

        // 初始化容器
        context.refresh(); // 执行 beanFactory 后处理器，添加 bea 后处理器，初始化所有单例

        log.debug("@ConfigurationProperties: {}", context.getBean(Bean4.class));

        // 销毁容器
        context.close();
    }
}
