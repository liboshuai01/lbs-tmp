package com.liboshuai.demo;

import com.liboshuai.demo.config.MyConfig;
import com.liboshuai.demo.service.OrderService;

public class Application
{
    public static void main( String[] args )
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        OrderService bean = (OrderService) context.getBean("orderServiceImpl");
        bean.test();
    }
}
