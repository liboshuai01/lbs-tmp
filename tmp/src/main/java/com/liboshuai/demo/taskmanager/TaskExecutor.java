package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.jobmanager.JobMasterGateway;
import com.liboshuai.demo.rpc.RpcEndpoint;
import com.liboshuai.demo.rpc.RpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutor extends RpcEndpoint implements TaskExecutorGateway {

    private static final String JOB_MASTER_ADDRESS = "pekko://job-manager@127.0.0.1:18888/user/job-master";

    private final JobMasterGateway jobMasterGateway;

    public TaskExecutor(RpcService rpcService, String endpointId) {
        super(rpcService, endpointId);

        jobMasterGateway = registerTaskExecutor(endpointId);
    }

    @Override
    public String queryTaskExecutorState() {
        return "状态正常";
    }

    private JobMasterGateway registerTaskExecutor(String endpointId) {
        JobMasterGateway jobMaster = getRpcService().connect(JOB_MASTER_ADDRESS, JobMasterGateway.class);
        String registerResult = jobMaster.registerTaskExecutor(endpointId, getRpcService().getAddress(endpointId));
        log.info("rpc远程注册taskExecutor到taskManager结果为：{}", registerResult);
        return jobMaster;
    }
}
