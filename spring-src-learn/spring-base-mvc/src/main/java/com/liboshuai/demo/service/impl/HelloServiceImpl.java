package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * Service接口的实现类。
 * 使用 @Service 注解，将其声明为Spring容器管理的一个Bean。
 */
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String getHelloMessage(String name) {
        // 模拟业务逻辑处理
        return "Hello, " + name + "! This is from native Spring.";
    }
}
