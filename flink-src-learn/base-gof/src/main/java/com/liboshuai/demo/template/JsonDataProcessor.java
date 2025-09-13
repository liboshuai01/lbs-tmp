package com.liboshuai.demo.template;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@Getter
public class JsonDataProcessor extends AbstractDataProcessor {

    private int userCount = 0;

    @Override
    protected List<User> parseData(String data) {
        log.info("2. [JSON解析器] - 正在解析JSON数据...");
        // 极简模拟解析，仅为演示模式
        if (data.contains("username") && data.contains("age")) {
            String name = data.substring(data.indexOf("username\":\"") + 11, data.indexOf("\","));
            int age = Integer.parseInt(data.substring(data.indexOf("age\":") + 5, data.indexOf("}")));
            return Collections.singletonList(new User(name, age));
        }
        return Collections.emptyList();
    }

    @Override
    protected void analyzeData(List<User> userList) {
        log.info("3. [JSON分析器] - 正在分析数据：统计用户数量...");
        this.userCount = userList.size();
        System.out.println("分析结果：用户数 = " + this.userCount);
    }
}
