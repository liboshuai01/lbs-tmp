package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.taskmanager.TaskExecutorGateway;
import lombok.Getter;

public class TaskExecutorRegister {
    @Getter
    private final String taskExecutorId;
    @Getter
    private final String address;
    @Getter
    private final TaskExecutorGateway taskExecutorGateway;

    public TaskExecutorRegister(String taskExecutorId, String address, TaskExecutorGateway taskExecutorGateway) {
        this.taskExecutorId = taskExecutorId;
        this.address = address;
        this.taskExecutorGateway = taskExecutorGateway;
    }
}
