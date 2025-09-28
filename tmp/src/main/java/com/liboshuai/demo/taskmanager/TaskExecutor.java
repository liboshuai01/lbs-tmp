package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.jobmanager.JobMasterGateway;
import com.liboshuai.demo.rpc.RpcEndpoint;
import com.liboshuai.demo.rpc.RpcGateway;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutor extends RpcEndpoint implements TaskExecutorGateway {

    // 待修改为中配置文件中获取
    private static final String JOB_MANAGER_ADDRESS = "pekko://job-manager@127.0.0.1:18888/user/job-master";

    private final JobMasterGateway jobMaster;

    public TaskExecutor(RpcService rpcService, String endpointId) {
        // 调用父类构造方法，完成初始化rpc准备的工作
        super(rpcService, endpointId);

        // 创建 taskExecutor 时，直接向 JobManager 进行注册
        jobMaster = registerTaskManager();
    }

    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟查询slot信息
     */
    @Override
    public String requestSlot() {
        RpcUtils.mockNetWorkTimeProcess(2);
        return String.format("这是[%s]的slot信息", getEndpointId());
    }

    /**
     * 实现提供给第三方调用的rpc接口方法
     * 这里模拟将作业提交到slot中
     */
    @Override
    public String submitTask(String task) {
        RpcUtils.mockNetWorkTimeProcess(4);
        return String.format("这是向[%s]提交[%s]的结果", getEndpointId(), task);
    }

    /**
     * 远程调用 JobMaster 提供的rpc方法
     * 这里模拟向 JobMaster 进行注册，并获取 JobMaster 的代理对象
     */
    private JobMasterGateway registerTaskManager() {
        JobMasterGateway jobMaster = getRpcService().connect(JOB_MANAGER_ADDRESS, JobMasterGateway.class);
        String result = jobMaster.registerTaskManager(getEndpointId(), getRpcService().getAddress(getEndpointId()));
        log.info("rpc-registerTaskManager结果：{}", result);
        return jobMaster;
    }

    /**
     * 远程调用 JobMaster 提供的rpc方法
     * 这里模拟向 JobMaster 查询指定 job 的详细信息
     */
    public void requestJobDetails(String jobId) {
        String result = jobMaster.requestJobDetails(jobId);
        log.info("rpc-requestJobDetails结果：{}", result);
    }

    /**
     * 远程调用 JobMaster 提供的rpc方法
     * 这里模拟向 JobMaster 查询指定 job 的状态信息
     */
    public void requestJobStatus(String jobId) {
        String result = jobMaster.requestJobStatus(jobId);
        log.info("rpc-requestJobStatus结果：{}", result);
    }
}
