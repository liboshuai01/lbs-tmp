package com.liboshuai.demo.observer;

/**
 * 主题（被观察者）接口，定义了管理观察者的核心方法。
 */
public interface OrderStatusSubject {
    /**
     * 注册（添加）一个观察者
     */
    void registerObserver(OrderStatusObserver observer);

    /**
     * 移除一个观察者。
     */
    void removeObserver(OrderStatusObserver observer);

    /**
     * 通知所有已注册的观察者。
     */
    void notifyObserver();
}
