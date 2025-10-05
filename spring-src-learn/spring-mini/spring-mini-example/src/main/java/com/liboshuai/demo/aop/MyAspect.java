package com.liboshuai.demo.aop;

import com.liboshuai.demo.Aspect;
import com.liboshuai.demo.Before;
import com.liboshuai.demo.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class MyAspect {

    private static final Logger LOG = LoggerFactory.getLogger(MyAspect.class);

    @Before("execution(* com.liboshuai.demo.service.impl.OrderServiceImpl")
    public void beforeMethod() {
        LOG.info("执行了beforeMethod方法");
    }
}
