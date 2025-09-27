package com.liboshuai.demo.rpc;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcUtils {
    public static RpcService createRpcService(Configuration configuration) {
        return null;
    }

    /**
     * 模拟远程rpc调用，网络产生的耗时，特意时间长一些，突显异步调用
     */
    public static void mockNetWorkTimeProcess(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("被中断，原因为：", e);
            Thread.currentThread().interrupt();
        }
    }
}
