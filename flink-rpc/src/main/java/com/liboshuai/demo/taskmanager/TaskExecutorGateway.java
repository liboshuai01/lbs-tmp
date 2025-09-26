package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.RpcGateway;

import java.util.concurrent.CompletableFuture;

public interface TaskExecutorGateway extends RpcGateway {

    public String queryState();

    public String submitTask(String task);

    public CompletableFuture<String> heartBeatFromJobManager(String payload);


}
