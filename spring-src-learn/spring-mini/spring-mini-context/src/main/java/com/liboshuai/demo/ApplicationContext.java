package com.liboshuai.demo;

import java.util.Map;

public interface ApplicationContext {

    Object getBean(String name);

    Map<String, BeanDefinition> getBeanDefinitionMap();

}
