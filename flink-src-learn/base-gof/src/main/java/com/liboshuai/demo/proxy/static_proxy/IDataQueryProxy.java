package com.liboshuai.demo.proxy.static_proxy;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class IDataQueryProxy implements IDataQuery {

    private final IDataQuery iDataQuery;

    public IDataQueryProxy(IDataQuery iDataQuery) {
        this.iDataQuery = iDataQuery;
    }

    @Override
    public String query(String queryKey) {

        log.info("执行查询之前进行日志落库");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("日志落库被中断", e);
            Thread.currentThread().interrupt();
        }

        String result = iDataQuery.query(queryKey);

        log.info("执行查询之后进行日志落库");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("日志落库被中断", e);
            Thread.currentThread().interrupt();
        }

        return "[Proxy Enhanced] " + result;
    }
}
