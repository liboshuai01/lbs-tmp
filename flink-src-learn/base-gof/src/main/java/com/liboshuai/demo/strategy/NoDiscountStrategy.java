package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

/**
 * 具体策略A：无优惠
 */
public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        // 不进行任何操作，直接返回原价
        return originalPrice;
    }
}
