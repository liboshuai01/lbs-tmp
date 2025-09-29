package com.liboshuai.demo.taskmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;

import java.util.Scanner;

public class TaskManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "task-manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        TaskExecutor taskExecutor = new TaskExecutor(rpcService, "task-executor");

        new Thread(() -> {
            System.out.println("控制台输入任意字符，远程查询一次作业状态");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String ignore = scanner.nextLine();
                taskExecutor.requeryJobStatus("job1");
                System.out.println("控制台输入任意字符，远程查询一次作业状态");
            }
        }, "控制台输入线程").start();
    }
}
