package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PendingPaymentState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.info("操作成功：订单[{}]已支付。", context.getOrderId());
        log.info("状态变更：待支付 -> 已支付（待发货）。");
        context.setState(new PaidState());
    }

    @Override
    public void ship(OrderContext context) {
        log.info("操作失败：订单[{}]未支付，无法发货。", context.getOrderId());
    }

    @Override
    public void confirm(OrderContext context) {
        log.info("操作失败：订单[{}]未支付，无法确认收货。", context.getOrderId());
    }

    @Override
    public void cancel(OrderContext context) {
        log.info("操作成：订单[{}]已取消。", context.getOrderId());
        log.info("状态变更：待支付 -> 已取消。");
        context.setState(new CancelledState());
    }
}
