package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.service.HelloService;
import org.springframework.stereotype.Service;

// 和原生Spring版本完全一样
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String getHelloMessage(String name) {
        return "Hello, " + name + "! This is from Spring Boot.";
    }
}
