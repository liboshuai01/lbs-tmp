package com.liboshuai.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面类。
 * @Aspect 注解表明这是一个切面类。
 * @Component 注解让Spring容器能够扫描并管理这个Bean。
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * 定义一个切点 (Pointcut)。
     * 这个切点表达式匹配 com.example.springnative.controller 包下的所有类的所有公共方法。
     * execution(* com.example.springnative.controller..*.*(..))
     * - execution(): 这是最常用的切点指示符，用于匹配方法执行。
     * - *: 匹配任何返回类型。
     * - com.example.springnative.controller..: 匹配controller包及其所有子包。
     * - *.*: 匹配包下所有类的所有方法。
     * - (..): 匹配任何数量的参数。
     */
    @Pointcut("execution(* com.liboshuai.demo.controller.HelloController..*.*(..))")
    public void controllerLog() {}

    /**
     * 前置通知 (Before advice)。
     * 在目标方法 (由controllerLog()切点定义) 执行之前执行。
     * @param joinPoint 连接点，可以获取到目标方法的信息，如方法名、参数等。
     */
    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) {
        System.out.println("AOP Before: 方法 " + joinPoint.getSignature().getName() + " 开始执行...");
        System.out.println("AOP Before: 参数列表: " + Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 后置通知 (After advice)。
     * 在目标方法执行之后执行，无论方法是正常返回还是抛出异常。
     * @param joinPoint 连接点。
     */
    @After("controllerLog()")
    public void doAfter(JoinPoint joinPoint) {
        System.out.println("AOP After: 方法 " + joinPoint.getSignature().getName() + " 执行完毕。");
    }
}
