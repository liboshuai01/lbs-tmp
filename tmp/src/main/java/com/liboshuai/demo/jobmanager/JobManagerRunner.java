package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Scanner;

@Slf4j
public class JobManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "job_manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        JobMaster jobMaster = new JobMaster(rpcService, "job_master");

        log.info("请输入执行命令：q执行查询，s执行提交，e执行退出");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (Objects.equals("q", line)) {
                jobMaster.requestTaskSlotFormExecutor("task-1");
            } else if (Objects.equals("s", line)) {
                jobMaster.submitJobToTaskExecutor("task-1", "hello-world task");
            } else if (Objects.equals("e", line)) {
                log.info("正在退出");
                break;
            }
        }
    }
}
