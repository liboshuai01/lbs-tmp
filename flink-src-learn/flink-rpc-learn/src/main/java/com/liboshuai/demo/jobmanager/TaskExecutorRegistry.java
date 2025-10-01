package com.liboshuai.demo.jobmanager;

import com.liboshuai.demo.taskmanager.TaskExecutorGateway;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutorRegistry {

    private String resourceId;
    private String address;
    private TaskExecutorGateway taskExecutor;

}
