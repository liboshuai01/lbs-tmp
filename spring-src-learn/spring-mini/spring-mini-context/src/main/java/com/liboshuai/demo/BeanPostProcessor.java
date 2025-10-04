package com.liboshuai.demo;

public interface BeanPostProcessor {

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
