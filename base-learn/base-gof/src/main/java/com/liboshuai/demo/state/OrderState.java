package com.liboshuai.demo.state;

public interface OrderState {
    void pay(OrderContext context);
    void ship(OrderContext context);
    void confirm(OrderContext context);
    void cancel(OrderContext context);
}
