package com.liboshuai.demo.template;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 抽象数据处理模板类
 * 定义了处理数据的标准流程（算法骨架）
 */
@Slf4j
public abstract class AbstractDataProcessor {
    /**
     * 模板方法，定义了整个数据处理的流程。
     * 声明为 final，防止子类重写，从而破坏算法结构。
     */
    public final void process(String data) {
        // 1. 步骤1：打开数据源（具体方法）
        openDataSource();

        // 2. 步骤2：解析数据（抽象方法，由子类实现）
        List<User> userList = parseData(data);

        // 3. 步骤3：分析数据（抽象方法，由子类实现）
        analyzeData(userList);

        // 4. 步骤4：钩子方法（提供默认实现，子类可选择性重写）
        afterProcessHook();

        // 5. 步骤5：关闭数据源（具体方法）
        closeDataSource();
    }

    // --- 具体方法（公共、不变的部分）---
    private void openDataSource() {
        log.info("1. 打开数据源...");
    }

    private void closeDataSource() {
        log.info("5. 关闭数据源...");
    }

    // --- 抽象方法（变化的部分，由子类实现）---

    /**
     * 解析数据，将输入字符串转换为User对象列表
     */
    protected abstract List<User> parseData(String data);

    /**
     * 分析处理数据，进行具体的业务逻辑
     */
    protected abstract void analyzeData(List<User> userList);

    // --- 钩子方法（Hook Method）---

    /**
     * 处理完成后的钩子方法
     * 默认实现为空，子类可以按需重写可以添加额外的行为。
     */
    protected void afterProcessHook() {
        // 默认无任何操作
    }
}
