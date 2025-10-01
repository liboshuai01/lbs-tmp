package com.liboshuai.demo.decorator.beverage;

import java.math.BigDecimal;

public class Espresso implements Beverage {
    @Override
    public String getDescription() {
        return "浓缩咖啡";
    }

    @Override
    public BigDecimal getPrice() {
        return new BigDecimal("3");
    }
}
