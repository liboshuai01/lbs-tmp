package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.RpcGateway;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface JobMasterGateway extends RpcGateway {

    String registerTaskExecutor(String taskExecutorAddress,String resourceId) throws ExecutionException, InterruptedException;

    CompletableFuture<String> getMasterId();
}
