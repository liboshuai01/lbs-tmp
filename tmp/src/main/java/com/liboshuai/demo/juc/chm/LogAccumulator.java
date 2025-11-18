package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class LogAccumulator {

    // 存储 TaskName -> 累积的日志内容
    private final ConcurrentHashMap<String, String> taskLogs = new ConcurrentHashMap<>();

    /**
     * 追加日志
     *
     * @param taskName 任务名称
     * @param newLog   新的日志片段
     * @return 追加后的完整日志
     */
    public String appendLog(String taskName, String newLog) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 如果 taskName 不存在，存入 newLog
        // 2. 如果存在，将 newLog 追加到旧值后面，使用 "\n" 分隔
        // 3. 返回合并后的最新结果
        return taskLogs.merge(taskName, newLog, (oldValue, newValue) -> String.join("\n", oldValue, newValue));
//        return null; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        LogAccumulator accumulator = new LogAccumulator();

        // 第一次添加
        accumulator.appendLog("Task-A", "Started");

        // 第二次添加，应该拼接
        String result = accumulator.appendLog("Task-A", "Processing");

        System.out.println("Result:\n" + result);
        // 预期输出:
        // Started
        // Processing
    }
}
