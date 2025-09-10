package com.liboshuai.demo.proxy.cglib_proxy;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DataQueryMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        log.info("执行查询之前进行日志落库");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("日志落库被中断", e);
            Thread.currentThread().interrupt();
        }

        Object result = methodProxy.invokeSuper(o, objects);

        log.info("执行查询之后进行日志落库");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("日志落库被中断", e);
            Thread.currentThread().interrupt();
        }
        if (result instanceof String) {
            return "[Proxy Enhanced] " + result;
        }
        return result;
    }
}
