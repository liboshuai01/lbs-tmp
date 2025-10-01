package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args) {
        // 1. 创建两个接收者
        FileIOService fileIOService = new FileIOService();
        LoggingService loggingService = new LoggingService();
        // 2. 创建调用者
        TaskExecutor taskExecutor = new TaskExecutor();
        // --- 经典实现方式 ---
        log.info("=== 使用经典实现方式添加任务 ===");
        // 3. 创建两个具体命令并设置接收者
        SaveFileTask saveFileTask = new SaveFileTask(fileIOService, "report.pdf");
        WriteLogTask writeLogTask = new WriteLogTask(loggingService, "INFO", "日志内容");
        // 4. 将两个命令交给调用者
        taskExecutor.addTask(saveFileTask);
        taskExecutor.addTask(writeLogTask);
        // 5. 调用者执行命令
        taskExecutor.executeTasks();
        log.info("");
        log.info("==================================");
        log.info("");

        // --- Java 8 Lambda 优化实现方式 ---
        log.info("=== 使用 Java 8 Lambda 优化方式添加任务 ===");
        // 3. 直接使用 Lambda 表达式创建两个命令对象
        Task saveFileTaskLambda = () -> fileIOService.saveFile("data.csv");
        Task writeLogTaskLambda = () -> loggingService.log("DEBUG", "日志内容");
        // 4. 将命令交给调用者
        taskExecutor.addTask(saveFileTaskLambda);
        taskExecutor.addTask(writeLogTaskLambda);
        taskExecutor.addTask(() -> log.info("任意的 lambda 任务！"));
        // 5. 调用者执行命令
        taskExecutor.executeTasks();
    }
}
