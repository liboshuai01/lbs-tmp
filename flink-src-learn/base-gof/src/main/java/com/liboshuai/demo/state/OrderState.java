package com.liboshuai.demo.state;

/**
 * 抽象订单状态接口
 * 定义了所有状态下可能的操作
 */
public interface OrderState {
    /**
     * 支付订单
     */
    void pay(OrderContext context);
    /**
     * 发货
     */
    void ship(OrderContext context);
    /**
     * 确认收货
     */
    void confirm(OrderContext context);
    /**
     * 取消订单
     */
    void cancel(OrderContext context);
}
