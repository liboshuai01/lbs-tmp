package com.liboshuai.demo.decorator.condiment;

import com.liboshuai.demo.decorator.beverage.Beverage;

public abstract class CondimentDecorator implements Beverage {
    protected final Beverage beverage;

    protected CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }

}
