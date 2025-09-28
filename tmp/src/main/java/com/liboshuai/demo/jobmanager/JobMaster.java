package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.RpcEndpoint;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;
import com.liboshuai.demo.taskmanager.TaskExecutorGateway;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobMaster extends RpcEndpoint implements JobMasterGateway {

    private final Map<String, TaskExecutorRegister> taskExecutorRegisterMap = new ConcurrentHashMap<>();

    public JobMaster(RpcService rpcService, String endpointId) {
        // 调用父类构造器完成rpc准备工作
        super(rpcService, endpointId);
    }

    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟接受指定taskExecutor的注册
     */
    @Override
    public String registerTaskManager(String taskExecutorId, String address) {
        RpcUtils.mockNetWorkTimeProcess(2);
        registerInternal(taskExecutorId, address);
        return "taskManager注册成功";
    }

    private void registerInternal(String taskExecutorId, String address) {
        if (taskExecutorRegisterMap.containsKey(taskExecutorId)) {
            throw new IllegalStateException("taskExecutor[" + taskExecutorId + "]" + "已经注册过了");
        }
        TaskExecutorGateway taskExecutor = getRpcService().connect(address, TaskExecutorGateway.class);
        TaskExecutorRegister taskExecutorRegister = new TaskExecutorRegister(taskExecutorId, address, taskExecutor);
        taskExecutorRegisterMap.put(taskExecutorId, taskExecutorRegister);
    }

    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟根据jobId查询job详情
     */
    @Override
    public String requestJobDetails(String jobId) {
        RpcUtils.mockNetWorkTimeProcess(2);
        return String.format("这是[%s]的Job详情", jobId);
    }

    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟根据jobId查询job状态
     */
    @Override
    public String requestJobStatus(String jobId) {
        RpcUtils.mockNetWorkTimeProcess(2);
        return String.format("这是[%s]的Job状态", jobId);
    }

    /**
     * 远程调用taskExecutor提供的rpc方法
     * 这里模拟查询指定taskId的TaskExecutor的slot信息
     */
    public void requestTaskSlotFormExecutor(String taskExecutorId) {
        TaskExecutorGateway taskExecutor = getTaskExecutorById(taskExecutorId);
        String response = taskExecutor.requestSlot();
        log.info("rpc-requestSlot结果：{}", response);
    }

    /**
     * 远程调用taskExecutor提供的rpc方法
     * 这里模拟向指定taskId的TaskExecutor提交job
     */
    public void submitJobToTaskExecutor(String taskExecutorId, String job) {
        TaskExecutorGateway taskExecutor = getTaskExecutorById(taskExecutorId);
        String response = taskExecutor.submitTask(job);
        log.info("rpc-submitTask结果：{}", response);
    }

    private TaskExecutorGateway getTaskExecutorById(String taskExecutorId) {
        if (!taskExecutorRegisterMap.containsKey(taskExecutorId)) {
            throw new IllegalStateException("taskExecutor[" + taskExecutorId + "]" + "还没有被注册");
        }
        TaskExecutorRegister taskExecutorRegister = taskExecutorRegisterMap.get(taskExecutorId);
        return taskExecutorRegister.getTaskExecutor();
    }

    @Override
    public String getAddress() {
        return "";
    }

    @Override
    public String getHostname() {
        return "";
    }
}
