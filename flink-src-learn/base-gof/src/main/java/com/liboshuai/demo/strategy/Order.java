package com.liboshuai.demo.strategy;

import lombok.Setter;

import java.math.BigDecimal;

/**
 * 环境类：订单
 * 它持有一个策略接口的引用，并将具体的计算行为委托给策略对象
 */
public class Order {
    private final BigDecimal originalPrice;
    /**
     * -- SETTER --
     *  允许在运行时动态更换优惠策略
     */
    @Setter
    private DiscountStrategy discountStrategy;

    public Order(BigDecimal originalPrice, DiscountStrategy discountStrategy) {
        this.originalPrice = originalPrice;
        this.discountStrategy = discountStrategy;
    }

    /**
     * 计算最终价格
     */
    public BigDecimal getFinalPrice() {
        // 调用策略对象的算法
        return discountStrategy.applyDiscount(originalPrice);
    }
}
