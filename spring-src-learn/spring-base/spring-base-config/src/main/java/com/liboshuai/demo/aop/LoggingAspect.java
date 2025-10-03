package com.liboshuai.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect     // 1. 使用@Aspect注解声明这是一个切面类（对应aop:config标签）
@Component  // 2. 使用@Component注解，让spring容器能够扫描到并创建这个Bean（对应bean标签）
public class LoggingAspect {

    // 3. 定义一个可重用的切点
    // @Pointcut注解对应xml标签：aop:pointcut
    @Pointcut("execution(* com.liboshuai.demo.service.UserService.addUser(..))")
    public void serviceLayerPointcut() {}

    // 4. 定义各种通知，并引用上面的切点
    // @Before注解对应xml标签：aop:before
    // 前置通知：在目标方法执行之前执行
    @Before("serviceLayerPointcut()")
    public void beforeAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-前置通知】: " + methodName + " 方法开始执行...");
    }

    // @After注解对应xml标签：aop:after
    // 后置通知：在目标方法执行之后执行（无论是否发生异常）
    @After("serviceLayerPointcut()")
    public void afterAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-后置通知】: " + methodName + " 方法执行结束.");
    }

    // @AfterReturning注解对应xml标签：aop:after-returning
    // 返回通知：在目标方法成功执行并返回结果后执行
    @AfterReturning(value = "serviceLayerPointcut()", returning = "result")
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-返回通知】: " + methodName + " 方法成功返回, 结果是: " + result);
    }

    // @AfterThrowing注解对应xml标签：aop:after-throwing
    // 异常通知：在目标方法抛出异常后执行
    @AfterThrowing(pointcut = "serviceLayerPointcut()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-异常通知】: " + methodName + " 方法执行时发生异常: " + ex.getMessage());
    }
}
