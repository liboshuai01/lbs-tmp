package com.liboshuai.demo;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AopBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AopBeanPostProcessor.class);

    private final List<Advisor> advisors = new ArrayList<>();

    /**
     * 通过传入的ApplicationContext获取所有注册的bean对象，过滤出aspect对象，将信息放入advisors中
     */
    public AopBeanPostProcessor(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitionMap = applicationContext.getBeanDefinitionMap();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            // 判断是否存在Aspect注解
            if (!beanDefinition.getBeanClass().isAnnotationPresent(Aspect.class)) {
                continue;
            }
            Method[] declaredMethods = beanDefinition.getBeanClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                method.setAccessible(true);
                // 判断方法上是否存在Before注解
                if (!method.isAnnotationPresent(Before.class)) {
                    continue;
                }
                Before beforeAnnotation = method.getAnnotation(Before.class);
                String pointcut = beforeAnnotation.value();
                Object aspect;
                try {
                     aspect = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                Advisor advisor = new Advisor(pointcut, method, aspect);
                advisors.add(advisor);
            }
        }
    }

    /**
     * 1. 判断符合pointCut要求的，才进行动态代理。表示需要拿到@Before注解的值
     * 2. 在执行被代理方法之前，需要先执行@Before注解修饰的方法体内容。表示需要拿到被@Before注解修饰的方法Method对象，以及所属类的对象实例
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        for (Advisor advisor : advisors) {
            // 判断符合pointCut要求的，才进行动态代理
            if (!advisor.getPointcut().contains(bean.getClass().getSimpleName())) {
                continue;
            }
            // 1. 创建 Enhancer 类
            Enhancer enhancer = new Enhancer();
            // 2. 设置父类（目标类），CGLIB是通过继承来实现的
            enhancer.setSuperclass(bean.getClass());
            // 3. 设置回调（方法拦截器）
            enhancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    // 通过反射执行Before中的增强方法
                    advisor.getMethod().invoke(advisor.getAspect(), null);
                    return method.invoke(bean, objects);
                }});
            // 4. 创建代理对象并返回
            return enhancer.create();
        }
        return bean;
    }

    /**
     * aop切面信息
     */
    static class Advisor {
        /**
         * Before注解中值，用于表示所要匹配的类
         */
        private final String pointcut;
        /**
         * Before注解修饰方法
         */
        private final Method method;
        /**
         * 切面类实例化对象
         */
        private final Object aspect;

        public String getPointcut() {
            return pointcut;
        }

        public Method getMethod() {
            return method;
        }

        public Object getAspect() {
            return aspect;
        }

        public Advisor(String pointcut, Method method, Object aspect) {
            this.pointcut = pointcut;
            this.method = method;
            this.aspect = aspect;
        }
    }

}
