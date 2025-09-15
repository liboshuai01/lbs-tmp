package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 具体的被观察者 - 订单
 */
@Slf4j
@Getter
@ToString
public class Order implements OrderStatusSubject {

    private final String orderId;
    private OrderStatus currentStatus;
    private final List<OrderStatusObserver> observers = new ArrayList<>();

    public Order(String orderId) {
        this.orderId = orderId;
        this.currentStatus = OrderStatus.PENDING_PAY;
    }

    public enum OrderStatus {
        PENDING_PAY, // 待支付
        PAID, // 已支付
        SHIPPED, // 已发货
        COMPLETED, // 已完成
        CANCELLED, // 已取消
    }

    @Override
    public void registerObserver(OrderStatusObserver observer) {
        if (observer == null) {
            log.warn("订单状态观察者对象为空，无法注册的到观察者列表中");
            return;
        }
        observers.add(observer);
    }

    @Override
    public void removeObserver(OrderStatusObserver observer) {
        if (observer == null) {
            log.warn("订单状态观察者对象为空，无法从观察者列表中移除");
            return;
        }
        observers.remove(observer);
    }

    @Override
    public void notifyObserver() {
        for (OrderStatusObserver observer : new ArrayList<>(observers)) {
            observer.onStatusUpdated(this);
        }
    }

    public void setStatus(OrderStatus newStatus) {
        if (Objects.equals(currentStatus, newStatus)) {
            log.warn("状态没有发生变更，无需通知任何观察者");
            return;
        }
        this.currentStatus = newStatus;
        notifyObserver();
    }
}
