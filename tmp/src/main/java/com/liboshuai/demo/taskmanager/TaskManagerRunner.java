package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;

public class TaskManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "task-manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        TaskExecutor taskExecutor = new TaskExecutor(rpcService, "task-executor");
    }
}
