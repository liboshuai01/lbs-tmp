package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.RpcGateway;

public interface TaskExecutorGateway extends RpcGateway {
    String requestSlot();
    String submitTask(String task);
}
