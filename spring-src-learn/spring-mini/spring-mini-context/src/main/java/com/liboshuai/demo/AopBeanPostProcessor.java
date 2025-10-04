package com.liboshuai.demo;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class AopBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AopBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // 1. 创建 Enhancer 类
        Enhancer enhancer = new Enhancer();
        // 2. 设置父类（目标类），CGLIB是通过继承来实现的
        enhancer.setSuperclass(bean.getClass());
        // 3. 设置回调（方法拦截器）
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                LOG.debug("{}-aop切面-执行方法之前",beanName);
                Object proxyBean = method.invoke(bean, objects);
                LOG.debug("{}-aop切面-执行方法之后", beanName);
                return proxyBean;
            }});
        // 4. 创建代理对象并返回
        return enhancer.create();
    }

}
