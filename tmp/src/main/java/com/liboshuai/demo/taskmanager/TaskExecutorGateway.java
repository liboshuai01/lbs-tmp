package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.RpcGateway;

public interface TaskExecutorGateway extends RpcGateway {
    String querySlot();
    String submitTask(String task);
}
