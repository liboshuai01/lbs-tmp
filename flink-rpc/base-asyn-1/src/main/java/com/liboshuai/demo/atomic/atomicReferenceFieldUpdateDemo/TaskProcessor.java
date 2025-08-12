package com.liboshuai.demo.atomic.atomicReferenceFieldUpdateDemo;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class TaskProcessor {

    private static final AtomicReferenceFieldUpdater<Task, TaskStatus> taskStatusUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Task.class, TaskStatus.class, "taskStatus");

    private static final AtomicReferenceFieldUpdater<Task, TaskResult> taskResultUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Task.class, TaskResult.class, "taskResult");

    public void process(Task task) {
        if (taskStatusUpdater.compareAndSet(task, TaskStatus.NEW,  TaskStatus.RUNNING)) {
            System.out.printf("线程 [%s]: 成功获取任务 '%s' 的执行权，状态更新为 RUNNING。%n",
                    Thread.currentThread().getName(), task.getTaskName());

        }
    }
}
