package com.liboshuai.demo.state;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单上下文（环境类）
 * 持有订单的当前状态，并将客户端请求委托给当前状态对象处理
 */
@Slf4j
@Getter
@ToString
public class OrderContext {
    private final String orderId;
    private OrderState currentState;

    public OrderContext(String orderId) {
        this.orderId = orderId;
        // 订单创建时，初始状态为“待支付”
        this.currentState = new PendingPaymentState();
        log.info("订单[{}]已创建，当前状态为：待支付。", orderId);
    }

    /**
     * 用于状态转换
     */
    public void setState(OrderState newState) {
        this.currentState = newState;
    }

    // --- 将行为委托给当前状态对象
    public void payOrder() {
        this.currentState.pay(this);
    }

    public void shipOrder() {
        this.currentState.ship(this);
    }

    public void confirmReceipt() {
        this.currentState.confirm(this);
    }

    public void cancelOrder() {
        this.currentState.cancel(this);
    }
}
