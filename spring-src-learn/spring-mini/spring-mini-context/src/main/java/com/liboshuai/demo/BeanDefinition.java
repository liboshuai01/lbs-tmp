package com.liboshuai.demo;

import java.util.Objects;

/**
 * Bean的定义信息
 */
public class BeanDefinition {
    /**
     * Bean的class信息
     */
    private Class<?> beanClass;
    /**
     * Bean的作用域：singleton-单例；prototype-多例
     */
    private String scope;
    /**
     * 是否为懒加载
     */
    private boolean isLazy;

    public BeanDefinition() {
    }

    public BeanDefinition(Class<?> beanClass, String scope, boolean isLazy) {
        this.beanClass = beanClass;
        this.scope = scope;
        this.isLazy = isLazy;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BeanDefinition that = (BeanDefinition) o;
        return isLazy == that.isLazy && Objects.equals(beanClass, that.beanClass) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClass, scope, isLazy);
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "beanClass=" + beanClass +
                ", scope='" + scope + '\'' +
                ", isLazy=" + isLazy +
                '}';
    }
}
