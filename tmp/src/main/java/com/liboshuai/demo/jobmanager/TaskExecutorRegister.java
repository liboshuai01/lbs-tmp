package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.taskmanager.TaskExecutorGateway;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskExecutorRegister {
    private String taskExecutorId;
    private String address;
    private TaskExecutorGateway taskExecutor;
}
