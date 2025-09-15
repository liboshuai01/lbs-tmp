package com.liboshuai.demo.observer;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 具体观察者 - 库存服务
 * 关系订单“已支付”状态，以便锁定库存。
 */
@Slf4j
@Getter
@ToString
public class InventoryService implements OrderStatusObserver {

    private String lastProcessedOrderIdForPayment;

    @Override
    public void onStatusUpdated(Order order) {
        // 库存服务只关心“已支付”状态
        if (order.getCurrentStatus() == Order.OrderStatus.PAID) {
           log.info("[库存服务] 接收到通知：订单[{}]已支付，正在锁定库存...", order.getOrderId());
           // 模拟业务逻辑
            lockInventory(order);
        }
    }

    private void lockInventory(Order order) {
        // 实际的库存锁定逻辑...
        this.lastProcessedOrderIdForPayment = order.getOrderId();
        log.info("[库存服务] 订单[{}]库存锁定成功。", order.getOrderId());
    }
}
