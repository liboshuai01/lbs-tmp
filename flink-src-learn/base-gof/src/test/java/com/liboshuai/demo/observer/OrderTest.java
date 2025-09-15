package com.liboshuai.demo.observer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private Order order;
    private InventoryService inventoryService;
    private ShippingService shippingService;
    private PushNotificationService pushNotificationService;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，都创建一个新的订单和观察者实例
        order = new Order("ORDER-12345");
        inventoryService = new InventoryService();
        shippingService = new ShippingService();
        pushNotificationService = new PushNotificationService();
    }

    @Test
    @DisplayName("测试观察者注册与移除")
    void testObserverRegistrationAndRemoval() {
        // 初始时，观察者列表应为空
        assertTrue(order.getObservers().isEmpty(), "新订单的观察者列表应该为空");

        // 注册一个观察者
        order.registerObserver(inventoryService);
        assertEquals(1, order.getObservers().size(), "注册后，观察者数量应为1");
        assertTrue(order.getObservers().contains(inventoryService), "观察者列表中应包含已注册的服务");

        // 移除观察者
        order.removeObserver(inventoryService);
        assertTrue(order.getObservers().isEmpty(), "移除后，观察者列表应该为空");
    }

    @Test
    @DisplayName("当订单状态变为 '已支付' 时，应通知库存和物流服务")
    void whenOrderStatusBecomesPaid_shouldNotifyInventoryAndShippingServices() {
        // 注册关心“已支付”状态的观察者
        order.registerObserver(inventoryService);
        order.registerObserver(shippingService);
        // 注册不关心“已支付”状态的观察者
        order.registerObserver(pushNotificationService);

        // 初始状态下，下游服务不应有任何处理记录
        assertNull(inventoryService.getLastProcessedOrderIdForPayment());
        assertNull(shippingService.getLastOrderIdForShippingCreation());
        assertNull(pushNotificationService.getLastNotifiedShipmentId());

        // 核心动作：改变订单状态为“已支付”
        order.setStatus(Order.OrderStatus.PAID);

        // 验证：库存和物流服务被正确调用
        assertEquals("ORDER-12345", inventoryService.getLastProcessedOrderIdForPayment(), "库存服务应该处理了该订单");
        assertEquals("ORDER-12345", shippingService.getLastOrderIdForShippingCreation(), "物流服务应该为该订单创建了发货单");

        // 验证：推送服务没有被调用，因为它不关心“已支付”状态
        assertNull(pushNotificationService.getLastNotifiedShipmentId(), "推送服务不应该被调用");
    }

    @Test
    @DisplayName("当订单状态变为 '已发货' 时，应通知推送服务")
    void whenOrderStatusBecomesShipped_shouldNotifyPushService() {
        order.registerObserver(pushNotificationService);

        assertNull(pushNotificationService.getLastNotifiedShipmentId());

        // 核心动作：改变订单状态为“已发货”
        order.setStatus(Order.OrderStatus.SHIPPED);

        // 验证：推送服务被正确调用
        assertEquals("ORDER-12345", pushNotificationService.getLastNotifiedShipmentId(), "推送服务应该发送了发货通知");
    }

    @Test
    @DisplayName("当设置的状态与当前状态相同时，不应发送任何通知")
    void whenStatusIsUnchanged_shouldNotNotifyObservers() {
        // 使用一个简单的Lambda观察者来测试是否被调用
        final boolean[] wasNotified = {false};
        OrderStatusObserver mockObserver = (o) -> wasNotified[0] = true;

        order.registerObserver(mockObserver);

        // 第一次设置状态，应该通知
        order.setStatus(Order.OrderStatus.PAID);
        assertTrue(wasNotified[0], "第一次状态变更时，观察者应该被通知");

        // 重置标志
        wasNotified[0] = false;

        // 再次设置相同的状态
        order.setStatus(Order.OrderStatus.PAID);

        // 验证：观察者未被再次通知
        assertFalse(wasNotified[0], "状态未改变时，观察者不应该被通知");
    }
}