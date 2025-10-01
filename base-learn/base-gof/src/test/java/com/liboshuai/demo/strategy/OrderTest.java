package com.liboshuai.demo.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("订单优惠策略测试")
class OrderTest {

    @Test
    @DisplayName("测试无优惠策略")
    void testNoDiscountStrategy() {
        BigDecimal price = new BigDecimal("100.00");
        Order order = new Order(price, new NoDiscountStrategy());
        // 使用 assertEquals(expected, actual, message) 进行断言，0表示两个BigDecimal值相等
        assertEquals(0, price.compareTo(order.getFinalPrice()), "无优惠时价格应该不变");
    }

    @Test
    @DisplayName("测试固定金额减免策略")
    void testFixedAmountDiscountStrategy() {
        BigDecimal price = new BigDecimal("150.50");
        BigDecimal discount = new BigDecimal("20.00");
        Order order = new Order(price, new FixedAmountDiscountStrategy(discount));
        BigDecimal expected = new BigDecimal("130.50");
        assertEquals(0, expected.compareTo(order.getFinalPrice()), "固定金额减免后价格不正确");
    }

    @Test
    @DisplayName("测试固定金额减免策略 - 优惠后价格为0")
    void testFixedAmountDiscountStrategyWhenPriceIsNegative() {
        BigDecimal price = new BigDecimal("10.00");
        BigDecimal discount = new BigDecimal("20.00");
        Order order = new Order(price, new FixedAmountDiscountStrategy(discount));
        // 价格不能为负，最低为0
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getFinalPrice()), "优惠后价格最低为0");
    }

    @Test
    @DisplayName("测试满减策略 - 满足条件")
    void testThresholdDiscountStrategyMet() {
        BigDecimal price = new BigDecimal("250.00");
        BigDecimal threshold = new BigDecimal("200.00");
        BigDecimal discount = new BigDecimal("30.00");
        Order order = new Order(price, new ThresholdDiscountStrategy(threshold, discount));
        BigDecimal expected = new BigDecimal("220.00");
        assertEquals(0, expected.compareTo(order.getFinalPrice()), "满足满减条件时价格计算错误");
    }

    @Test
    @DisplayName("测试满减策略 - 不满足条件")
    void testThresholdDiscountStrategyNotMet() {
        BigDecimal price = new BigDecimal("199.99");
        BigDecimal threshold = new BigDecimal("200.00");
        BigDecimal discount = new BigDecimal("30.00");
        Order order = new Order(price, new ThresholdDiscountStrategy(threshold, discount));
        // 不满足条件，价格应不变
        assertEquals(0, price.compareTo(order.getFinalPrice()), "不满足满减条件时价格不应改变");
    }

    @Test
    @DisplayName("测试运行时动态切换策略")
    void testDynamicStrategyChange() {
        BigDecimal price = new BigDecimal("300.00");
        Order order = new Order(price, new NoDiscountStrategy());
        assertEquals(0, new BigDecimal("300.00").compareTo(order.getFinalPrice()), "初始应为无优惠");

        // 切换到满减策略
        order.setDiscountStrategy(new ThresholdDiscountStrategy(new BigDecimal("200"), new BigDecimal("30")));
        assertEquals(0, new BigDecimal("270.00").compareTo(order.getFinalPrice()), "切换到满减策略后价格计算错误");

        // 再次切换到Lambda表示的9折策略
        order.setDiscountStrategy(p -> p.multiply(new BigDecimal("0.9")));
        assertEquals(0, new BigDecimal("270.00").compareTo(order.getFinalPrice()), "切换到9折策略后价格计算错误");
    }

    @Test
    @DisplayName("测试使用 Lambda 表达式作为策略")
    void testLambdaAsStrategy() {
        BigDecimal price = new BigDecimal("1000.00");
        // 定义一个8折优惠
        DiscountStrategy vipDiscount = originalPrice -> originalPrice.multiply(new BigDecimal("0.8"));
        Order order = new Order(price, vipDiscount);
        BigDecimal expected = new BigDecimal("800.00");
        assertEquals(0, expected.compareTo(order.getFinalPrice()), "使用Lambda作为策略时价格计算错误");
    }
}