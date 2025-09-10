package com.liboshuai.demo.decorator.beverage;

import java.math.BigDecimal;

/**
 * 饮品接口
 */
public interface Beverage {
    /**
     * 获取饮品的描述
     */
    String getDescription();

    /**
     * 获取饮品的价格
     */
    BigDecimal getPrice();
}
