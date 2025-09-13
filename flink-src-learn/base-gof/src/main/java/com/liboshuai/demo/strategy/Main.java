package com.liboshuai.demo.strategy;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class Main {
    public static void main(String[] args) {
        // 场景1：一个原价为 500 元的订单
        BigDecimal price = new BigDecimal("500");
        Order order = new Order(price, new NoDiscountStrategy());
        log.info("无优惠时，最终价格：{}", order.getFinalPrice());

        // 2. 场景2：应用“满200减30”的优惠
        order.setDiscountStrategy(new ThresholdDiscountStrategy(new BigDecimal("200"), new BigDecimal("30")));
        log.info("满200减30后，最终价格：{}", order.getFinalPrice());

        // 3. 应用“新人立减10元”的优惠
        order.setDiscountStrategy(new FixedAmountDiscountStrategy(new BigDecimal("10")));
        log.info("新人立减10元后，最终价格：{}", order.getFinalPrice());

        // --- Java 1.8 Lambda 表达式的威力 ---
        // 场景4：VPI 会员直接打9折，无需创建新类，直接用 Lambda 定义策略
        order.setDiscountStrategy(originalPrice -> originalPrice.multiply(new BigDecimal("0.9")));
        log.info("VIP会员打9折后，最终价格：{}", order.getFinalPrice());

        // 场景5：一个更复杂的临时活动，如“超级会员日，所有商品打8折后再减5元”
        order.setDiscountStrategy(originalPrice -> {
            BigDecimal discountedPrice = originalPrice.multiply(new BigDecimal("0.8"));
            return discountedPrice.subtract(new BigDecimal("5"));
        });
        log.info("超级会员日活动后，最终价格：{}", order.getFinalPrice());
    }
}
