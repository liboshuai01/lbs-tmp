package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;

import java.util.concurrent.ExecutionException;

public class TaskManagerRunner {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Configuration configuration = new Configuration();
        configuration.setProperty("actor.system.name","taskmanager");

        RpcService rpcService = RpcUtils.createRpcService(configuration);

        TaskExecutor taskExecutor = new TaskExecutor(rpcService,"task-executor");

    }

}
