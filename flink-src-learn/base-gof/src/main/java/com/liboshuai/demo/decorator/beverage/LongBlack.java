package com.liboshuai.demo.decorator.beverage;

import java.math.BigDecimal;

public class LongBlack implements Beverage {
    @Override
    public String getDescription() {
        return "美式咖啡";
    }

    @Override
    public BigDecimal getPrice() {
        return new BigDecimal("5");
    }
}
