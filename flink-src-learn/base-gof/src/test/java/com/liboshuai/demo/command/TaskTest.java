package com.liboshuai.demo.command;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("具体命令类测试")
class TaskTest {

    @Mock
    private FileIOService mockFileIOService; // 模拟的接收者1

    @Mock
    private LoggingService mockLoggingService; // 模拟的接收者2

    @Test
    @DisplayName("SaveFileTask应正确调用FileIOService的saveFile方法")
    void saveFileTaskShouldCallReceiver() {
        // 准备: 创建具体命令，并注入模拟的接收者
        String fileName = "test.txt";
        SaveFileTask saveFileTask = new SaveFileTask(mockFileIOService, fileName);

        // 执行: 调用命令的execute方法
        saveFileTask.execute();

        // 验证: 验证模拟的FileIOService的saveFile方法是否被以正确的参数调用了1次
        verify(mockFileIOService, times(1)).saveFile(fileName);
    }

    @Test
    @DisplayName("WriteLogTask应正确调用LoggingService的log方法")
    void writeLogTaskShouldCallReceiver() {
        // 准备
        String level = "DEBUG";
        String message = "Test message";
        WriteLogTask writeLogTask = new WriteLogTask(mockLoggingService, level, message);

        // 执行
        writeLogTask.execute();

        // 验证
        verify(mockLoggingService, times(1)).log(level, message);
    }
}