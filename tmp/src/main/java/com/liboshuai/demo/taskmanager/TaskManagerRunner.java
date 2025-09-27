package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Scanner;

@Slf4j
public class TaskManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "task-manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        TaskExecutor taskexecutor = new TaskExecutor(rpcService, "task-executor");

        log.info("请输入执行命令：s查询状态，d查询信息，e执行退出");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (Objects.equals("s", line)) {
                taskexecutor.requestJobStatus("job1");
            } else if (Objects.equals("d", line)) {
                taskexecutor.requestJobDetails("job1");
            } else if (Objects.equals("e", line)) {
                log.info("正在退出");
                break;
            }
        }
    }
}
