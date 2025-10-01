package com.liboshuai.demo.bridge.shape;

import com.liboshuai.demo.bridge.color.Color;

public class Square extends Shape {
    protected Square(Color color) {
        super(color);
    }

    @Override
    public String draw() {
        return "绘制正方形，并" + color.applyColor();
    }
}
