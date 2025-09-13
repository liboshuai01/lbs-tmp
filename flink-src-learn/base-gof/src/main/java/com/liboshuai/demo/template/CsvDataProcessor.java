package com.liboshuai.demo.template;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class CsvDataProcessor extends AbstractDataProcessor {

    private int totalAge = 0; // 用于存储分析结果

    @Override
    protected List<User> parseData(String data) {
        log.info("2. [CSV解析器] - 正在解析CSV数据...");
        // 简单的CSV解析：按行分割，再按逗号分割
        return Arrays.stream(data.split("\n"))
                .map(line -> {
                    String[] fields = line.split(",");
                    return new User(fields[0].trim(), Integer.parseInt(fields[1].trim()));
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void analyzeData(List<User> userList) {
        log.info("3. [CSV分析器] - 正在分析数据：计算用户总年龄...");
        this.totalAge = userList.stream().mapToInt(User::getAge).sum();
        System.out.println("分析结果：总年龄 = " + this.totalAge);
    }

    @Override
    protected void afterProcessHook() {
        log.info("4. [CSV钩子] - CSV数据处理完毕，记录日志...");
    }
}
