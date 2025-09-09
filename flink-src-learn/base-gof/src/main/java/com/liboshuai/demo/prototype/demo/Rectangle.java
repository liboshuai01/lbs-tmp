package com.liboshuai.demo.prototype.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Rectangle extends Shape{
    public Rectangle() {
        type = "Rectangle";
    }
    @Override
    void draw() {
      log.info("正在绘制一个矩形...");
    }
}
