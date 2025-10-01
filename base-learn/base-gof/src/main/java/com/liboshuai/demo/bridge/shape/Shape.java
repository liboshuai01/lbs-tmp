package com.liboshuai.demo.bridge.shape;

import com.liboshuai.demo.bridge.color.Color;

public abstract class Shape {
    protected final Color color;

    protected Shape(Color color) {
        this.color = color;
    }

    public abstract String draw();
}
