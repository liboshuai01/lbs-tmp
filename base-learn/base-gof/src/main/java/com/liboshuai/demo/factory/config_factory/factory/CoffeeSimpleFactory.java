package com.liboshuai.demo.factory.config_factory.factory;


import com.liboshuai.demo.factory.config_factory.coffee.Coffee;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CoffeeSimpleFactory {

    private static final Map<String, Coffee> BEAN_MAP = new HashMap<>();

    static {
        Properties props = new Properties();
        try (InputStream is = CoffeeSimpleFactory.class.getClassLoader().getResourceAsStream("bean.properties")) {
            if (is == null) {
                throw new IllegalStateException("配置文件 'bean.properties' 未在classpath中找到！");
            }
            props.load(is);
            for (String key : props.stringPropertyNames()) {
                String className = props.getProperty(key);
                Class<?> clazz = Class.forName(className);
                Coffee coffee = (Coffee) clazz.newInstance();
                BEAN_MAP.put(key, coffee);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("初始化CoffeeFactory失败，请检查配置文件或类路径。错误详情: " + e.getMessage());
        }
    }

    public Coffee createCoffee(String type) {
        return BEAN_MAP.get(type);
    }
}
