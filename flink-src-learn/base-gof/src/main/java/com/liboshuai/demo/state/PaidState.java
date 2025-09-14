package com.liboshuai.demo.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaidState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        log.info("操作失败：订单[{}]已支付，请勿重复支付。", context.getOrderId());
    }

    @Override
    public void ship(OrderContext context) {
        log.info("操作成功：订单[{}]已发货。", context.getOrderId());
        log.info("状态变更：已支付 -> 已发货。");
        context.setState(new ShippedState());
    }

    @Override
    public void confirm(OrderContext context) {
        log.info("操作失败：订单[{}]尚未发货，无法确认收货。", context.getOrderId());
    }

    @Override
    public void cancel(OrderContext context) {
        log.info("操作失败：订单[{}]已支付，无法直接取消，请联系客服处理退款。", context.getOrderId());
    }
}
