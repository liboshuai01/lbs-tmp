package com.liboshuai.demo.strategy;

import lombok.Setter;

import java.math.BigDecimal;

public class Order {
    private final BigDecimal originalPrice;
    @Setter
    private DiscountStrategy discountStrategy;

    public Order(BigDecimal originalPrice, DiscountStrategy discountStrategy) {
        this.originalPrice = originalPrice;
        this.discountStrategy = discountStrategy;
    }

    public BigDecimal getFinalPrice() {
        return discountStrategy.applyDiscount(originalPrice);
    }
}
