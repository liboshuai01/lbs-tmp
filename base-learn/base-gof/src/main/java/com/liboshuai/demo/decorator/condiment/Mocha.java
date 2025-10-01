package com.liboshuai.demo.decorator.condiment;

import com.liboshuai.demo.decorator.beverage.Beverage;

import java.math.BigDecimal;

public class Mocha extends CondimentDecorator {
    public Mocha(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + "，加摩卡";
    }

    @Override
    public BigDecimal getPrice() {
        return beverage.getPrice().add(new BigDecimal("0.3"));
    }
}
