package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShippedState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.info("操作失败：订单 [{}] 已支付。", context.getOrderId());
    }

    @Override
    public void ship(OrderContext context) {
        log.info("操作失败：订单 [{}] 已发货，请勿重复发货。", context.getOrderId());
    }

    @Override
    public void confirm(OrderContext context) {
        log.info("操作成功：订单 [{}] 已确认收货。", context.getOrderId());
        log.info("状态变更：已发货 -> 已完成。");
        context.setState(new CompletedState());
    }

    @Override
    public void cancel(OrderContext context) {
        log.info("操作失败：订单 [{}] 已发货，无法取消。", context.getOrderId());
    }
}
