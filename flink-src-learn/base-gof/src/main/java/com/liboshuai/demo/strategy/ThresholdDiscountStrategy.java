package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

/**
 * 具体策略C：满减优惠
 */
public class ThresholdDiscountStrategy implements DiscountStrategy {

    private final BigDecimal threshold; // 门槛金额
    private final BigDecimal discountAmount; // 优惠金额

    public ThresholdDiscountStrategy(BigDecimal threshold, BigDecimal discountAmount) {
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        // 如果原价达到了门槛金额，则减去优惠金额
        if (originalPrice.compareTo(threshold) >= 0) {
            return originalPrice.subtract(discountAmount);
        }
        return originalPrice;
    }
}
