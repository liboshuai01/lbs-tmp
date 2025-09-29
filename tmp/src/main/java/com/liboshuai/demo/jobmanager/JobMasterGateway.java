package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.RpcGateway;

public interface JobMasterGateway extends RpcGateway {
    String registerTaskExecutor(String taskExecutorId, String address);
    String requestJobStatus(String jobId);
}
