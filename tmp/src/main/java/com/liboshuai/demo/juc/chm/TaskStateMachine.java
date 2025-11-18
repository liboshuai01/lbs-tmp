package com.liboshuai.demo.juc.chm;

import java.util.concurrent.ConcurrentHashMap;

public class TaskStateMachine {

    public enum TaskState {
        CREATED, DEPLOYING, RUNNING, FINISHED, FAILED, CANCELED
    }

    // 存储 TaskID -> 当前状态
    private final ConcurrentHashMap<String, TaskState> taskStates = new ConcurrentHashMap<>();

    // 初始化测试数据
    public TaskStateMachine() {
        taskStates.put("task-1", TaskState.DEPLOYING);
        taskStates.put("task-2", TaskState.CANCELED);
    }

    /**
     * 尝试将任务状态从 DEPLOYING 切换到 RUNNING
     * * @param taskId 任务ID
     * @return true 表示切换成功；false 表示失败（例如当前状态不是 DEPLOYING）
     */
    public boolean switchToRunning(String taskId) {
        // TODO: 请在这里编写代码
        // 要求：
        // 1. 只有当当前状态严格等于 DEPLOYING 时，才更新为 RUNNING
        // 2. 返回操作是否成功
        // 3. 必须原子操作
        return taskStates.replace(taskId, TaskState.DEPLOYING, TaskState.RUNNING);
//        return false; // 占位符
    }

    // --- 测试辅助代码 ---
    public static void main(String[] args) {
        TaskStateMachine sm = new TaskStateMachine();

        // 场景 1: 状态是 DEPLOYING，应该成功
        boolean success1 = sm.switchToRunning("task-1");
        System.out.println("Task-1 switch result: " + success1); // 预期: true
        System.out.println("Task-1 current state: " + sm.taskStates.get("task-1")); // 预期: RUNNING

        // 场景 2: 状态是 CANCELED，应该失败
        boolean success2 = sm.switchToRunning("task-2");
        System.out.println("Task-2 switch result: " + success2); // 预期: false
        System.out.println("Task-2 current state: " + sm.taskStates.get("task-2")); // 预期: CANCELED
    }
}
