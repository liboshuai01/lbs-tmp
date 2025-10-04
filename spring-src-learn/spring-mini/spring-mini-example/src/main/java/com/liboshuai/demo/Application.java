package com.liboshuai.demo;

import com.liboshuai.demo.config.MyConfig;
import com.liboshuai.demo.service.UserService;

public class Application
{
    public static void main( String[] args )
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        UserService bean = (UserService) context.getBean("userService");
        bean.test();
        System.out.println(context.getBean("userService"));
        System.out.println(context.getBean("userService"));
        System.out.println(context.getBean("userService"));
    }
}
