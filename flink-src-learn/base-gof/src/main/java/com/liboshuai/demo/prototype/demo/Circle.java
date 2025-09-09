package com.liboshuai.demo.prototype.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Circle extends Shape{
    public Circle() {
        type = "Circle";
    }

    @Override
    void draw() {
        log.info("正在绘制一个圆形...");
    }
}
