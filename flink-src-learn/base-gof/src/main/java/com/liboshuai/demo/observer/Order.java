package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 具体的被观察者 - 订单类。
 * 当其状态发生变化时，会通知所有注册的观察者。
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
        this.currentStatus = OrderStatus.PENDING_PAYMENT;
    }

    public enum OrderStatus {
        PENDING_PAYMENT, // 待支付
        PAID, // 已支付
        SHIPPED, // 已发货
        COMPLETED, // 已完成
        CANCELLED, // 已取消

    }

    @Override
    public void registerObserver(OrderStatusObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(OrderStatusObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObserver() {
        // 遍历并通知每一个观察者
        // 创建一个副本进行遍历，防止在通知过程中观察者列表被修改（例如，某个观察者在update方法中移除了自己）
        for (OrderStatusObserver observer : new ArrayList<>(observers)) {
            observer.onStatusUpdated(this);
        }
    }

    /**
     * 核心业务方法：更新订单状态。
     * 状态变更后，立即通知所有观察者。
     */
    public void setStatus(OrderStatus newStatus) {
        // 只有当状态真正改变时才通知，避免不必要的操作
        if (newStatus != this.currentStatus) {
            log.info("订单 [{}] 状态从 [{}] 变为 [{}]", orderId, this.currentStatus, newStatus);
            this.currentStatus = newStatus;
            // 状态变更，通知所有依赖方
            notifyObserver();
        }
    }
}
