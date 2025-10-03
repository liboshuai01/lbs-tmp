package com.liboshuai.demo.aop;

import org.aspectj.lang.JoinPoint;

public class LoggingAspect {

    // 前置通知：在目标方法执行之前执行
    public void beforeAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-前置通知】: " + methodName + " 方法开始执行...");
    }

    // 后置通知：在目标方法执行之后执行（无论是否发生异常）
    public void afterAdvice(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-后置通知】: " + methodName + " 方法执行结束.");
    }

    // 返回通知：在目标方法成功执行并返回结果后执行
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-返回通知】: " + methodName + " 方法成功返回, 结果是: " + result);
    }

    // 异常通知：在目标方法抛出异常后执行
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("【AOP-异常通知】: " + methodName + " 方法执行时发生异常: " + ex.getMessage());
    }
}
