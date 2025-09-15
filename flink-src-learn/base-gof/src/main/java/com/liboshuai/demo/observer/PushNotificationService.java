package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 具体观察者 - 推送通知服务。
 * 关心订单“已发货”状态，以便通知用户。
 */
@Slf4j
@Getter
@ToString
public class PushNotificationService implements OrderStatusObserver {

    private String lastNotifiedShipmentId;

    @Override
    public void onStatusUpdated(Order order) {
        // 推送服务只关心“已发货”状态
        if (order.getCurrentStatus() == Order.OrderStatus.SHIPPED) {
            log.info("[推送服务] 接收到通知：订单[{}]已发货，正在给用户发送推送...", order.getOrderId());
            sendPushNotification(order);
        }
    }

    private void sendPushNotification(Order order) {
        // 实际的推送逻辑...
        this.lastNotifiedShipmentId = order.getOrderId();
        log.info("[推送服务] 订单[{}] 发货通知推送成功。", order.getOrderId());
    }
}
