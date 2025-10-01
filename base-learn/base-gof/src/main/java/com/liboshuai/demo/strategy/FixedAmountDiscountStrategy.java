package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

public class FixedAmountDiscountStrategy implements DiscountStrategy {

    private final BigDecimal discountAmount;

    public FixedAmountDiscountStrategy(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);
        return finalPrice.compareTo(BigDecimal.ZERO) > 0 ? finalPrice : BigDecimal.ZERO;
    }
}
