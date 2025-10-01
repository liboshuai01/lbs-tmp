package com.liboshuai.demo.proxy.cglib_proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CglibProxyFactoryTest {
    @Test
    void test() {
        IDataQueryImpl iDataQuery = CglibProxyFactory.createProxy(IDataQueryImpl.class);
        String result = iDataQuery.query("lbs");
        assertEquals("[Proxy Enhanced] lbs real result", result);
    }
}