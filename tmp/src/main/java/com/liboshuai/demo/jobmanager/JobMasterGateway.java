package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.RpcGateway;

public interface JobMasterGateway extends RpcGateway {
    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟接受指定taskExecutor的注册
     */
    String registerTaskManager(String taskExecutorId, String address);
    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟根据jobId查询job详情
     */
    String requestJobDetails(String jobId);
    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟根据jobId查询job状态
     */
    String requestJobStatus(String jobId);
}
