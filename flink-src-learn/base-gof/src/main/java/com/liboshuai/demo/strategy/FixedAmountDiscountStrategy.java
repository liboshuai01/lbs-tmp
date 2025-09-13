package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

/**
 * 具体策略B：固定金额减免
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {
    private final BigDecimal discountAmount;

    public FixedAmountDiscountStrategy(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        // 减去固定金额，如果结果小于0，则返回0
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);
        return finalPrice.compareTo(BigDecimal.ZERO) > 0 ? finalPrice : BigDecimal.ZERO;
    }
}
