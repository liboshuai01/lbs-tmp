package com.liboshuai.demo;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App 
{
    public static void main( String[] args ) throws NoSuchFieldException, IllegalAccessException {
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        /*
         * 1. 到底什么是 BeanFactory
         * - 它是 ApplicationContext 的父接口
         * - 它才是 Spring 的核心容器，主要的 ApplicationContext 实现都是【组合】了它的功能。
         */
//        System.out.println(context);
        /*
            2. BeanFactory 能干点啥
                - 表面上看只有 getBean
                - 实际上控制反转、基本的依赖注入、直到 Bean 的声明周期的各种功能，都由它的实现类提供
         */
        Field singletonObjects = DefaultSingletonBeanRegistry.class.getDeclaredField("singletonObjects");
        singletonObjects.setAccessible(true);
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Map<String,Object> map = (Map<String, Object>) singletonObjects.get(beanFactory);
        map.forEach((k,v) -> {
            System.out.println(k + "=" + v);
        });
    }
}
