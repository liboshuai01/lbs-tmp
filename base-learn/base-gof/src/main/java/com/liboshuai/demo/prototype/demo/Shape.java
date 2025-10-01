package com.liboshuai.demo.prototype.demo;

import lombok.Data;

@Data
public abstract class Shape implements Cloneable {

    private String id;
    protected String type;

    abstract void draw();

    @Override
    protected Shape clone() throws CloneNotSupportedException {
        return (Shape) super.clone();
    }
}
