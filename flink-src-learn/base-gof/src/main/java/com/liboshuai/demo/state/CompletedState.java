package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompletedState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.info("无效操作：订单 [{}] 已完成。", context.getOrderId());
    }

    @Override
    public void ship(OrderContext context) {
        log.info("无效操作：订单 [{}] 已完成。", context.getOrderId());
    }

    @Override
    public void confirm(OrderContext context) {
        log.info("无效操作：订单 [{}] 已完成。", context.getOrderId());
    }

    @Override
    public void cancel(OrderContext context) {
        log.info("无效操作：订单 [{}] 已完成。", context.getOrderId());
    }
}
