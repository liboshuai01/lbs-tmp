package com.liboshuai.demo.observer;

/**
 * 被观察者接口
 */
public interface OrderStatusSubject {
    void registerObserver(OrderStatusObserver observer);

    void removeObserver(OrderStatusObserver observer);

    void notifyObserver();
}
