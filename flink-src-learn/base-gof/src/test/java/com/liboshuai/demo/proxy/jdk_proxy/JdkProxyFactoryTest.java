package com.liboshuai.demo.proxy.jdk_proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JdkProxyFactoryTest {

    @Test
    void test() {
        IDataQuery iDataQuery = JdkProxyFactory.createProxy(new IDataQueryImpl());
        String result = iDataQuery.query("lbs");
        assertEquals("[Proxy Enhanced] lbs real result", result);
    }
}