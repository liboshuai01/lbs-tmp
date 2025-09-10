package com.liboshuai.demo.proxy.cglib_proxy;

import net.sf.cglib.proxy.Enhancer;

public class CglibProxyFactory {
    public static <T> T createProxy(Class<T> targetClass) {
        // 1. 创建 Enhancer 类
        Enhancer enhancer = new Enhancer();
        // 2. 设置父类（目标类），CGLIB是通过继承来实现的
        enhancer.setSuperclass(targetClass);
        // 3. 设置回调（方法拦截器）
        enhancer.setCallback(new DataQueryMethodInterceptor());
        // 4. 创建代理对象并返回
        return (T) enhancer.create();
    }
}
