package com.liboshuai.demo.controller;

import com.liboshuai.demo.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Web控制器，处理HTTP请求。
 * @RestController 是 @Controller 和 @ResponseBody 的组合注解。
 * 它表示这个类中的所有方法默认返回JSON或XML格式的数据，而不是视图名。
 */
@RestController
public class HelloController {

    // 依赖注入 (IoC): Spring容器会自动将HelloService的实例注入到这里。
    @Autowired
    private HelloService helloService;

    /**
     * 处理对 "/hello" 路径的GET请求。
     * @param name 请求参数，例如 /hello?name=World
     * @return 一个Map对象，Spring MVC会通过Jackson自动将其序列化为JSON字符串。
     */
    @GetMapping("/hello")
    public Map<String, String> sayHello(@RequestParam(defaultValue = "World") String name) {
        String message = helloService.getHelloMessage(name);
        // 使用Collections.singletonMap创建一个只包含一个键值对的Map
        return Collections.singletonMap("message", message);
    }
}
