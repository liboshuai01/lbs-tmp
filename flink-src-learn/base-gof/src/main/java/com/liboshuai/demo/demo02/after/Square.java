package com.liboshuai.demo.demo02.after;

public class Square implements Quadrilateral{

    private double side;


    @Override
    public void setLength(double length) {
        this.side = length;
    }

    @Override
    public void setWidth(double width) {
        this.side = width;
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
