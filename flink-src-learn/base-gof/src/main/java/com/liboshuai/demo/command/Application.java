package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args) {
        // 1. 创建接收者
        FileIOService fileIOService = new FileIOService();
        LoggingService loggingService = new LoggingService();

        // 2. 创建调用者
        TaskExecutor taskExecutor = new TaskExecutor();

        // --- 经典实现方式 ---
        log.info("=== 使用经典实现方式添加任务 ===");
        // 3. 创建具体命令并设置接收者
        SaveFileTask saveTask = new SaveFileTask(fileIOService, "report.pdf");
        WriteLogTask logTask = new WriteLogTask(loggingService, "INFO", "A new report has been generated.");

        // 4. 将命令交给调用者
        taskExecutor.addTask(saveTask);
        taskExecutor.addTask(logTask);

        // 5. 调用者执行命令
        taskExecutor.executeTasks();

        log.info("");
        log.info("==================================");
        log.info("");

        // --- Java 8 Lambda 优化实现方式 ---
        log.info("=== 使用 Java 8 Lambda 优化方式添加任务 ===");
        // 3. 直接使用 Lambda 表达式创建命令对象
        Task saveTaskLambda = () -> fileIOService.saveFile("data.csv");
        Task logTaskLambda = () -> loggingService.log("WARN", "Disk space is running low.");

        // 4. 将命令交给调用者
        taskExecutor.addTask(saveTaskLambda);
        taskExecutor.addTask(logTaskLambda);
        taskExecutor.addTask(() -> log.info("一个简单的 Lambda 任务执行了！"));

        // 5. 调用者执行命令
        taskExecutor.executeTasks();
    }
}
