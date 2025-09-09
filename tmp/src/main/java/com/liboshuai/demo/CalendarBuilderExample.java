package com.liboshuai.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarBuilderExample {
    public static void main(String[] args) {
        // 使用 Calendar.Builder 创建一个代表 2025年9月7日上午10:30:00 的实例
        Calendar calendar = new Calendar.Builder()
                .setCalendarType("iso8601")
                .set(Calendar.YEAR, 2025)
                .set(Calendar.MONTH, Calendar.SEPTEMBER) // 月份从0开始，但这里用常量更清晰
                .set(Calendar.DAY_OF_MONTH, 7)
                .set(Calendar.HOUR_OF_DAY, 10)
                .set(Calendar.MINUTE, 30)
                .set(Calendar.SECOND, 0)
                .setLocale(Locale.CHINA)
                .build();

        Date date = calendar.getTime();
        System.out.println("通过Builder创建的Date: " + date);
    }
}