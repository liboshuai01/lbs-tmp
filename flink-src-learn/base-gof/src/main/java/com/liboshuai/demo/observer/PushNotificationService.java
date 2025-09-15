package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 具体观察者 - 信息推送服务
 */
@Slf4j
@Getter
@ToString
public class PushNotificationService implements OrderStatusObserver {

    private String lastProcessOrderId;

    @Override
    public void onStatusUpdated(Order order) {
        if (order.getCurrentStatus() != Order.OrderStatus.SHIPPED) {
            return;
        }
        pushNotification(order);
    }

    private void pushNotification(Order order) {
        log.info("[推送服务] 接收到通知：订单[{}]已经完成了发货，现在进行推送...", order.getOrderId());
        this.lastProcessOrderId = order.getOrderId();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
