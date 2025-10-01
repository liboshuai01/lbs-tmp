package com.liboshuai.demo.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class AbstractDataProcessorTest {

    @Test
    @DisplayName("测试CSV处理器-应能正确解析并计算总年龄")
    void testCsvDataProcessor() {
        // 准备 - Arrange
        String csvData = "Alice, 30\n" +
                "Bob, 25\n" +
                "Charlie, 35";
        CsvDataProcessor csvProcessor = new CsvDataProcessor();

        // 执行 - Act
        // 调用模板方法，整个流程会自动执行
        csvProcessor.process(csvData);

        // 验证 - Assert
        // 我们只关心最终的分析结果是否正确
        int expectedTotalAge = 30 + 25 + 35;
        assertEquals(expectedTotalAge, csvProcessor.getTotalAge(),  "CSV处理器计算的总年龄不正确");
    }

    @Test
    @DisplayName("测试JSON处理器 - 应能正确解析并统计用户数")
    void testJsonDataProcessor() {
        // 准备 - Arrange
        String jsonData = "{\"username\":\"David\", \"age\":40}";
        JsonDataProcessor jsonProcessor = new JsonDataProcessor();

        // 执行 - Act
        jsonProcessor.process(jsonData);

        // 验证 - Assert
        assertEquals(1, jsonProcessor.getUserCount(), "JSON处理器计算的用户数不正确");
    }

    @Test
    @DisplayName("测试JSON处理器 - 对于空数据应返回0个用户")
    void testJsonDataProcessorWithEmptyData() {
        // 准备 - Arrange
        String emptyJsonData = "{}";
        JsonDataProcessor jsonProcessor = new JsonDataProcessor();

        // 执行 - Act
        jsonProcessor.process(emptyJsonData);

        // 验证 - Assert
        assertEquals(0, jsonProcessor.getUserCount(), "JSON处理器处理空数据时用户数应为0");
    }
  
}