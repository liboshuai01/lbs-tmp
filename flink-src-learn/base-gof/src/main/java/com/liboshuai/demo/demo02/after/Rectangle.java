package com.liboshuai.demo.demo02.after;

public class Rectangle implements Quadrilateral {

    private double length;
    private double width;

    @Override
    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }
}
