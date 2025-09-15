package com.liboshuai.demo.state;

import lombok.Data;

@Data
public class OrderContext {
    private final long orderId;
    private OrderState currentState;

    public OrderContext(long orderId) {
        this.orderId = orderId;
        this.currentState = new PendingPaymentState();
    }

    public void pay() {
        this.currentState.pay(this);
    }

    public void ship() {
        this.currentState.ship(this);
    }

    public void confirm() {
        this.currentState.confirm(this);
    }

    public void cancel() {
        this.currentState.cancel(this);
    }

}
