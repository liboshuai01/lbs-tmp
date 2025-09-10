package com.liboshuai.demo.proxy.jdk_proxy;

import java.lang.reflect.Proxy;

public class JdkProxyFactory {
    public static IDataQuery createProxy(IDataQuery target) {
        return (IDataQuery) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new DataQueryInvocationHandler(target)
        );
    }
}
