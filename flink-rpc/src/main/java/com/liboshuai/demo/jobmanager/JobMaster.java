package com.liboshuai.demo.jobmanager;


import com.liboshuai.demo.rpc.RpcEndpoint;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.taskmanager.TaskExecutorGateway;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class JobMaster extends RpcEndpoint implements JobMasterGateway {

    ConcurrentHashMap<String, TaskExecutorRegistry> registeredExecutors = new ConcurrentHashMap<>();

    HeartBeatManager heartBeatManager;

    public JobMaster(RpcService rpcService, String endpointId) {
        // 调父类构造，以准备各种rpc基础功能
        super(rpcService, endpointId);

        // 是模块自身的相关构造逻辑

        // 初始化心跳服务管理组件
        heartBeatManager = new HeartBeatManager(new HeartBeatListener(),getMainThreadExecutor());

    }


    // 提供给taskExecutor来调用的rpc方法
    @Override
    public String registerTaskExecutor(String taskExecutorAddress, String resourceId) throws ExecutionException, InterruptedException {

        if (registeredExecutors.containsKey(resourceId)) {
            return "注册重复";
        }


        String s = registerInternal(resourceId, taskExecutorAddress);

        // 向刚注册的taskExecutor查询状态信息
        TaskExecutorGateway taskExecutor = registeredExecutors.get(resourceId).getTaskExecutor();

        String state = taskExecutor.queryState();
        System.out.println("查询到刚注册的taskExecutor的状态信息: " + state);


        // 将当前注册的taskExecutor放入心跳监控池
        heartBeatManager.monitHeartBeatTarget(resourceId,taskExecutor);



        return s;
    }

    @Override
    public CompletableFuture<String> getMasterId() {

        return CompletableFuture.supplyAsync(() -> "master-1");
    }


    public String registerInternal(String resourceId, String taskExecutorAddress) throws ExecutionException, InterruptedException {

        System.out.println("收到taskExecutor注册信息: " + taskExecutorAddress);
        TaskExecutorGateway taskExecutor = rpcService.connect(taskExecutorAddress, TaskExecutorGateway.class);

        registeredExecutors.put(resourceId, new TaskExecutorRegistry(resourceId, taskExecutorAddress, taskExecutor));

        return "注册通过";
    }


    // 对taskExecutor发起请求的方法
    public String queryTaskExecutorState(String taskExecutorId) {

        TaskExecutorGateway taskExecutor = registeredExecutors.get(taskExecutorId).getTaskExecutor();
        String state = taskExecutor.queryState();

        System.out.println(state);

        return state;
    }


    // 对taskExecutor发起请求的方法
    public String submitTask(String task, String taskExecutorId) {

        TaskExecutorGateway taskExecutor = registeredExecutors.get(taskExecutorId).getTaskExecutor();
        String response = taskExecutor.submitTask(task);

        System.out.println(response);

        return response;
    }


    public void disconnectTaskManager(String resourceId){

        System.out.println(resourceId + ",心跳失败次数超过阈值,从注册池中移除,master内移除," + Thread.currentThread());
        registeredExecutors.remove(resourceId);
        heartBeatManager.removeHeartBeatTarget(resourceId);
    }


    public class HeartBeatListener {

        public void notifyHeartBeatFailure(String resourceId){
            disconnectTaskManager(resourceId);
        }

    }


}
