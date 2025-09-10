package com.liboshuai.demo.decorator.condiment;

import com.liboshuai.demo.decorator.beverage.Beverage;

import java.math.BigDecimal;

public class Milk extends CondimentDecorator{

    public Milk(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + "，加牛奶";
    }

    @Override
    public BigDecimal getPrice() {
        return beverage.getPrice().add(new BigDecimal("0.5"));
    }
}
