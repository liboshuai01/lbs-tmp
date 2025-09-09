package com.liboshuai.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class FormatterBuilderExample {
    public static void main(String[] args) {
        // 需求：创建一个格式，如 "2025年-09月-09日 星期二 18点"
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)       // 添加年份
                .appendLiteral("年-")                // 添加静态文字
                .appendValue(ChronoField.MONTH_OF_YEAR, 2) // 添加月份，占2位
                .appendLiteral("月-")
                .appendValue(ChronoField.DAY_OF_MONTH, 2)  // 添加日期，占2位
                .appendLiteral("日 ")
                .appendText(ChronoField.DAY_OF_WEEK) // 添加星期几的文字
                .appendLiteral(" ")
                .appendValue(ChronoField.HOUR_OF_DAY, 2)   // 添加小时，占2位
                .appendLiteral("点")
                .toFormatter();                      // 构建最终的 Formatter

        LocalDateTime now = LocalDateTime.now();
        System.out.println("使用Builder构建的格式: " + now.format(formatter));
    }
}
