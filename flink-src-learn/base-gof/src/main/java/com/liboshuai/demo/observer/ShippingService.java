package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 具体观察者 - 物流服务。
 * 关心订单“已支付”状态，以便创建发货单。
 */
@Slf4j
@Getter
@ToString
public class ShippingService implements OrderStatusObserver{

    private String lastOrderIdForShippingCreation;

    @Override
    public void onStatusUpdated(Order order) {
        // 物流服务也只关心“已支付”状态
        if (order.getCurrentStatus() == Order.OrderStatus.PAID) {
            log.info("[物流服务] 接收到通知：订单[{}]已支付，正在创建发货单...", order.getOrderId());
            createShipment(order);
        }
    }

    private void createShipment(Order order) {
        // 实际的创建发货单逻辑...
        this.lastOrderIdForShippingCreation = order.getOrderId();
        log.info("[物流服务] 订单[{}]发货单创建成功。", order.getOrderId());
    }
}
