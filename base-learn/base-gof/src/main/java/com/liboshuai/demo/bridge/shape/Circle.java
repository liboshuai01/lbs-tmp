package com.liboshuai.demo.bridge.shape;

import com.liboshuai.demo.bridge.color.Color;

public class Circle extends Shape {
    public Circle(Color color) {
        super(color);
    }

    @Override
    public String draw() {
        return "绘制圆形，并" + color.applyColor();
    }
}
