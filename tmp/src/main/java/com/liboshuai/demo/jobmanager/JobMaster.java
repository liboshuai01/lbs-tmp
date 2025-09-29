package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.RpcEndpoint;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.taskmanager.TaskExecutorGateway;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JobMaster extends RpcEndpoint implements JobMasterGateway {

    private final Map<String, TaskExecutorRegister> taskExecutorRegisterMap = new HashMap<>();

    public JobMaster(RpcService rpcService, String endpointId) {
        super(rpcService, endpointId);

    }

    @Override
    public String registerTaskExecutor(String taskExecutorId, String address) {
        TaskExecutorGateway taskExecutorGateway = registerInternal(taskExecutorId, address);
        String taskExecutorState = taskExecutorGateway.queryTaskExecutorState();
        log.info("rpc查询taskExecutor状态结果：{}", taskExecutorState);
        return "注册成功";
    }

    private TaskExecutorGateway registerInternal(String taskExecutorId, String address) {
        if (taskExecutorRegisterMap.containsKey(taskExecutorId)) {
            throw new IllegalStateException("[" + taskExecutorId + "]已经注册过了，无法重复注册");
        }
        TaskExecutorGateway taskExecutorGateway = getRpcService().connect(address, TaskExecutorGateway.class);
        TaskExecutorRegister taskExecutorRegister = new TaskExecutorRegister(taskExecutorId, address, taskExecutorGateway);
        taskExecutorRegisterMap.put(taskExecutorId, taskExecutorRegister);
        return taskExecutorGateway;
    }
}
