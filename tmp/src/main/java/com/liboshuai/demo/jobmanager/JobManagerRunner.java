package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;

public class JobManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "job-manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        JobMaster jobMaster = new JobMaster(rpcService, "job-master");
    }
}
