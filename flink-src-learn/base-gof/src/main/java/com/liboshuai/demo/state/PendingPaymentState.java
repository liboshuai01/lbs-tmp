package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PendingPaymentState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.info("订单[{}]，支付成功，[待支付]状态变更为[已支付]状态。", context.getOrderId());
        context.setCurrentState(new PaidState());
    }

    @Override
    public void ship(OrderContext context) {
        log.warn("订单[{}]，发货失败，[待支付]状态下无法进行发货操作。", context.getOrderId());
    }

    @Override
    public void confirm(OrderContext context) {
        log.warn("订单[{}]，收货失败，[待支付]状态下无法进行收货操作。", context.getOrderId());
    }

    @Override
    public void cancel(OrderContext context) {
        log.info("订单[{}]，取消成功，[待支付]状态变更为[已取消]状态。", context.getOrderId());
        context.setCurrentState(new CancelledState());
    }
}
