package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.rpc.Configuration;
import com.liboshuai.demo.rpc.RpcService;
import com.liboshuai.demo.rpc.RpcUtils;

import java.util.Scanner;

public class JobManagerRunner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setProperties("actor.system.name", "job-manager");
        RpcService rpcService = RpcUtils.createRpcService(configuration);
        JobMaster jobMaster = new JobMaster(rpcService, "job-master");

        new Thread(() -> {
            System.out.println("控制台输入任意字符，远程提交一次任务");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String ignore = scanner.nextLine();
                jobMaster.submitTask("task-executor", "hello world task");
                System.out.println("控制台输入任意字符，远程提交一次任务");
            }
        }, "控制台输入线程").start();
    }
}
