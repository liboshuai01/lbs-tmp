package com.liboshuai.demo.controller;

import com.liboshuai.demo.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

// 和原生Spring版本完全一样
@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @GetMapping("/hello")
    public Map<String, String> sayHello(@RequestParam(defaultValue = "World") String name) {
        String message = helloService.getHelloMessage(name);
        return Collections.singletonMap("message", message);
    }
}
