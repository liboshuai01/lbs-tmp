package com.liboshuai.slr.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SlrJucApplication {
    public static void main(String[] args) {
        // 建议设置 JVM 参数: -Xmx4g -Xms4g -XX:+UseG1GC
        SpringApplication.run(SlrJucApplication.class, args);
    }
}