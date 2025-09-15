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
        order = new Order("ORDER-202509151303");
        inventoryService = new InventoryService();
        shippingService = new ShippingService();
        pushNotificationService = new PushNotificationService();
    }

    @Test
    @DisplayName("测试观察者注册与移除")
    void testObserverRegistrationAndRemoval() {
        // 初始时，观察者列表应为空
        assertTrue(order.getObservers().isEmpty());
        // 注册一个观察者
        order.registerObserver(inventoryService);
        assertEquals(1, order.getObservers().size());
        // 移除观察者
        order.removeObserver(inventoryService);
        assertEquals(0, order.getObservers().size());
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
        assertNull(inventoryService.getLastProcessOrderId());
        assertNull(shippingService.getLastProcessOrderId());
        assertNull(pushNotificationService.getLastProcessOrderId());
        // 核心动作：改变订单状态为“已支付”
        order.setStatus(Order.OrderStatus.PAID);
        // 验证：库存和物流服务被正确调用
        assertEquals("ORDER-202509151303", inventoryService.getLastProcessOrderId());
        assertEquals("ORDER-202509151303", shippingService.getLastProcessOrderId());
        // 验证：推送服务没有被调用，因为它不关心“已支付”状态
        assertNull(pushNotificationService.getLastProcessOrderId());
    }

    @Test
    @DisplayName("当订单状态变为 '已发货' 时，应通知推送服务")
    void whenOrderStatusBecomesShipped_shouldNotifyPushService() {
        order.registerObserver(pushNotificationService);
        // 核心动作：改变订单状态为“已发货”
        order.setStatus(Order.OrderStatus.SHIPPED);
        // 验证：推送服务被正确调用
        assertEquals("ORDER-202509151303", pushNotificationService.getLastProcessOrderId());
    }
}