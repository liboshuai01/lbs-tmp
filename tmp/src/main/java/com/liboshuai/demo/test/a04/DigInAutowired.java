package com.liboshuai.demo.test.a04;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.env.StandardEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DigInAutowired {

    private static final Logger log = LoggerFactory.getLogger(DigInAutowired.class);

    public static void main(String[] args) throws Throwable {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("bean2", new Bean2());
        beanFactory.registerSingleton("bean3", new Bean3()); // 创建过程，依赖注入，初始化
        beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver()); // 提供 @Value 注解功能
        beanFactory.addEmbeddedValueResolver(new StandardEnvironment()::resolvePlaceholders);

        // 1. 查找哪些属性、方法加了 @Autowired，这称之为 InjectionMetadata
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(beanFactory);

        Bean1 bean1 = new Bean1();
//        log.debug("bean1 before: {}", bean1);
//        processor.postProcessProperties(null, bean1, "bean1"); // 执行依赖注入 @Autowired @Value 能力
//        log.debug("bean1 after: {}", bean1);

        Method method = AutowiredAnnotationBeanPostProcessor.class.getDeclaredMethod("findAutowiringMetadata", String.class, Class.class, PropertyValues.class);
        method.setAccessible(true);
        InjectionMetadata injectionMetadata = (InjectionMetadata) method.invoke(processor, "bean1", Bean1.class, null);
        log.debug("injectionMetadata: {}", injectionMetadata);

        // 2. 调用 InjectionMetadata 来进行依赖注入，注入是按类型查找值
        injectionMetadata.inject(bean1, "bean1", null);
        log.debug("bean1: {}", bean1);

        // 3. 如何按类型查找值
        Field bean3 = Bean1.class.getDeclaredField("bean3");
        DependencyDescriptor dd1 = new DependencyDescriptor(bean3, false);
        Object o = beanFactory.doResolveDependency(dd1, null, null, null);
        log.debug("o: {}", o);
    }
}
