package com.liboshuai.demo;

import java.time.Duration;
import java.time.Instant;

public class JvmDemo {
    public static void main(String[] args) {
        Instant start = Instant.now();
        JvmDemo jvmDemo = new JvmDemo();
        jvmDemo.method1(100000);
//        jvmDemo.method2(100000);
        Instant end = Instant.now();
        long timeElapsed = Duration.between(start, end).toMillis();
        System.out.println("timeElapsed: " + timeElapsed);
    }

    public void method1(int highLevel) {
        String src = "";
        for (int i = 0; i < highLevel; i++) {
            src = src + "a"; // 每次循环都会创建一个 StringBuilder和String对象
        }
    }

    public void method2(int highLevel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highLevel; i++) {
            sb.append("a");
        }
    }
}