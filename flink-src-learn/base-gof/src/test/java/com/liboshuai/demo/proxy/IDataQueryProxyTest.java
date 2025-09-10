package com.liboshuai.demo.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDataQueryProxyTest {

    @Test
    void test() {
        IDataQuery iDataQuery = new IDataQueryProxy(new IDataQueryImpl());
        String result = iDataQuery.query("lbs");
        assertEquals("[Proxy Enhanced] lbs real result", result);
    }
}