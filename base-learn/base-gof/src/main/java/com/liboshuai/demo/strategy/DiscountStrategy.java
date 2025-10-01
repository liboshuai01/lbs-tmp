package com.liboshuai.demo.strategy;

import java.math.BigDecimal;

@FunctionalInterface
public interface DiscountStrategy {

    BigDecimal applyDiscount(BigDecimal originalPrice);
}
