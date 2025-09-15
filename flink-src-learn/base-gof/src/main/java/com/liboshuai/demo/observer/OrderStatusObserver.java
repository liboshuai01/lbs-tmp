package com.liboshuai.demo.observer;

/**
 * 观察者接口，定义了当订单状态更新时需要执行的动作。
 */
@FunctionalInterface
public interface OrderStatusObserver {
    /**
     * 当被观察的订单状态发生变化时，此方法被调用。
     */
    void onStatusUpdated(Order order);
}
