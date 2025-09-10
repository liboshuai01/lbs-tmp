package com.liboshuai.demo.flyweight;

public class Circle implements Shape{

    private final String color;

    public Circle(String color) {
        this.color = color;
    }

    @Override
    public String draw(int x, int y) {
        return String.format("圆形，颜色：%s，坐标：(%d, %d)", color, x, y);
    }
}
