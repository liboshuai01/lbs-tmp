package com.liboshuai.demo.proxy.jdk_proxy;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class IDataQueryImpl implements IDataQuery {
    @Override
    public String query(String queryKey) {
        log.info("执行了真实的查询操作");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("查询被中断", e);
            Thread.currentThread().interrupt();
        }
        return queryKey + " real result";
    }
}
