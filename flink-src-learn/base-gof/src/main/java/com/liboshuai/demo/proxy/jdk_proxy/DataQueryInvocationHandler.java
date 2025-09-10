package com.liboshuai.demo.proxy.jdk_proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DataQueryInvocationHandler implements InvocationHandler {

    private final Object object;

    public DataQueryInvocationHandler(Object object) {
        this.object = object;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("执行查询之前进行日志落库");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("日志落库被中断", e);
            Thread.currentThread().interrupt();
        }

        Object result = method.invoke(object, args);

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
