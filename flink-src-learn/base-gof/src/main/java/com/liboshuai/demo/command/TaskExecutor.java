package com.liboshuai.demo.command;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TaskExecutor {
    private final List<Task> taskQueue = new ArrayList<>();

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    public void executeTasks() {
        log.info("--- 开始执行任务队列 ---");
        for (Task task : taskQueue) {
            task.execute();
        }
        log.info("--- 所有任务执行完毕 ---");
        taskQueue.clear();
    }
}
