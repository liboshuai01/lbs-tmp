package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.RpcGateway;

import java.util.concurrent.CompletableFuture;

public interface TaskExecutorGateway extends RpcGateway {

    String queryState();

    String submitTask(String task);

    CompletableFuture<String> heartBeatFromJobManager(String payload);

}
