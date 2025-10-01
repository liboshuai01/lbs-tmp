package com.liboshuai.demo.observer;

/**
 * 观察者接口
 */
public interface OrderStatusObserver {
    void onStatusUpdated(Order order);
}
