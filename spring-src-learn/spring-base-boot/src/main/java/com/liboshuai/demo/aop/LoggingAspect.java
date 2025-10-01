package com.liboshuai.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.Arrays;

// 和原生Spring版本几乎完全一样 (只需修改切点表达式中的包名)
@Aspect
@Component
public class LoggingAspect {

    // 切点表达式需要更新为Spring Boot项目的正确包名
    @Pointcut("execution(* com.liboshuai.demo.controller..*.*(..))")
    public void controllerLog() {}

    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) {
        System.out.println("AOP Before: 方法 " + joinPoint.getSignature().getName() + " 开始执行...");
        System.out.println("AOP Before: 参数列表: " + Arrays.toString(joinPoint.getArgs()));
    }

    @After("controllerLog()")
    public void doAfter(JoinPoint joinPoint) {
        System.out.println("AOP After: 方法 " + joinPoint.getSignature().getName() + " 执行完毕。");
    }
}
