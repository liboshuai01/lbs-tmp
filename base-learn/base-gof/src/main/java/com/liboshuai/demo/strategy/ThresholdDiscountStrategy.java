package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

public class ThresholdDiscountStrategy implements DiscountStrategy {

    private final BigDecimal threshold;
    private final BigDecimal discountAmount;

    public ThresholdDiscountStrategy(BigDecimal threshold, BigDecimal discountAmount) {
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        if (threshold.compareTo(discountAmount) < 0) {
            throw new IllegalArgumentException("门槛金额必须大于优惠金额");
        }
        if (originalPrice.compareTo(threshold) >= 0) {
            return originalPrice.subtract(discountAmount);
        }
        return originalPrice;
    }
}
