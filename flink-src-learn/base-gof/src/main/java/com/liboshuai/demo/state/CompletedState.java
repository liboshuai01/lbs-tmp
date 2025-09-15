package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompletedState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.warn("订单[{}]，支付失败，[已完成]状态下无法进行支付操作。", context.getOrderId());
    }

    @Override
    public void ship(OrderContext context) {
        log.warn("订单[{}]，发货失败，[已完成]状态下无法进行发货操作。", context.getOrderId());
    }

    @Override
    public void confirm(OrderContext context) {
        log.warn("订单[{}]，收货失败，[已完成]状态下无法进行收货操作。", context.getOrderId());
    }

    @Override
    public void cancel(OrderContext context) {
        log.warn("订单[{}]，取消失败，[已完成]状态下无法进行取消操作。", context.getOrderId());
    }
}
