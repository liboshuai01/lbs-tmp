package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        return originalPrice;
    }
}
