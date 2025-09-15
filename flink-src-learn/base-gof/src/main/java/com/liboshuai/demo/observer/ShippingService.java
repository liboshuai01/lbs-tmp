package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 具体观察者 - 物流服务
 */
@Slf4j
@Getter
@ToString
public class ShippingService implements OrderStatusObserver {

    private String lastProcessOrderId;

    @Override
    public void onStatusUpdated(Order order) {
        if (order.getCurrentStatus() != Order.OrderStatus.PAID) {
            return;
        }
        shipping(order);
    }

    private void shipping(Order order) {
        log.info("[物流服务] 接收到通知：订单[{}]已经完成了支付，现在进行发货...", order.getOrderId());
        this.lastProcessOrderId = order.getOrderId();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
