package com.liboshuai.demo.atomic.atomicReferenceFieldUpdateDemo;

public class Task {
    private final String taskName;

    public volatile TaskStatus taskStatus = TaskStatus.NEW;

    public volatile TaskResult taskResult;

    public Task(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskName='" + taskName + '\'' +
                '}';
    }
}
