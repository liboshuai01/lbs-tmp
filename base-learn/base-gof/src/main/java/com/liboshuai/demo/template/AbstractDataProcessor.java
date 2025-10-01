package com.liboshuai.demo.template;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractDataProcessor {

    public final void process(String data) {
        // 1. 打开数据源（具体方法）
        openDataSource();
        // 2. 解析数据（抽象方法）
        List<User> userList = parseData(data);
        // 3. 处理数据（抽象方法）
        analyzeData(userList);
        // 4. 钩子方法（提供默认实现，子类可以选择性重写）
        afterProcessHook();
        // 5. 关闭数据源
        closeDataSource();
    }

    private void openDataSource() {
        log.info("1. 打开数据源");
    }

    protected abstract List<User> parseData(String data);

    protected abstract void analyzeData(List<User> userList);

    protected void afterProcessHook() {
        // 默认进行空实现
    }

    private void closeDataSource() {
        log.info("5. 关闭数据源");
    }
}
