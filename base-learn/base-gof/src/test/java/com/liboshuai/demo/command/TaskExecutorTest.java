package com.liboshuai.demo.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("调用者 TaskExecutor 测试")
class TaskExecutorTest {

    @InjectMocks
    private TaskExecutor taskExecutor; // 被测试的调用者

    @Mock
    private Task mockTask1; // 模拟的命令1

    @Mock
    private Task mockTask2; // 模拟的命令2

    @Test
    @DisplayName("当执行任务时，队列中所有任务的execute方法都应被调用")
    void executeTasksShouldCallExecuteOnAllTasksInQueue() {
        // 准备: 向调用者添加模拟的任务
        taskExecutor.addTask(mockTask1);
        taskExecutor.addTask(mockTask2);

        // 执行: 调用者的执行方法
        taskExecutor.executeTasks();

        // 验证: 验证队列中每个模拟任务的execute方法都被调用了1次
        verify(mockTask1, times(1)).execute();
        verify(mockTask2, times(1)).execute();
    }
}
