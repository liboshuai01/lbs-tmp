package com.liboshuai.demo.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Web应用的初始化类，作用类似于web.xml。
 * 它继承了AbstractAnnotationConfigDispatcherServletInitializer，
 * Servlet 3.0+规范的容器（如Tomcat 7+）会自动扫描并加载这个类，从而启动Spring应用。
 * 这是完全替代web.xml的Java配置方式。
 */
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * 配置Spring的根容器 (Root ApplicationContext)。
     * 加载非Web层的组件，如Service、Repository等。
     * @return 返回包含配置类的Class数组。
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { AppConfig.class };
    }

    /**
     * 配置Spring MVC的子容器 (Servlet WebApplicationContext)。
     * 加载Web层的组件，如Controller、HandlerMapping、HandlerAdapter等。
     * @return 返回包含配置类的Class数组。
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    /**
     * 配置DispatcherServlet的URL映射。
     * "/" 表示拦截所有进入应用的请求（除了.jsp等）。
     * @return 返回URL映射的字符串数组。
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
