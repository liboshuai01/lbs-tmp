package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

/**
 * 抽象策略接口：定义了计算优惠的统一方法
 */
@FunctionalInterface
public interface DiscountStrategy {
    /**
     * 根据原价计算优惠后的价格
     */
    BigDecimal applyDiscount(BigDecimal originalPrice);
}
