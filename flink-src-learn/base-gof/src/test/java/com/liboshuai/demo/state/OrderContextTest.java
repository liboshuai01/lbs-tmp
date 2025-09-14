package com.liboshuai.demo.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("订单状态模式测试")
class OrderContextTest {

    private OrderContext orderContext;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，都创建一个新的订单
        orderContext = new OrderContext("ORD20250914001");
    }

    @Test
    @DisplayName("测试订单完整生命周期：创建 -> 支付 -> 发货 -> 确认收货 -> 完成")
    void testOrderFullLifecycle() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState(), "订单初始状态应为待支付");

        // 2. 支付订单
        orderContext.payOrder();
        assertInstanceOf(PaidState.class, orderContext.getCurrentState(), "支付后状态应变为已支付");

        // 3. 发货
        orderContext.shipOrder();
        assertInstanceOf(ShippedState.class, orderContext.getCurrentState(), "发货后状态应变为已发货");

        // 4. 确认收货
        orderContext.confirmReceipt();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "确认收货后状态应变为已完成");
    }

    @Test
    @DisplayName("测试订单创建后直接取消")
    void testOrderCancellation() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState(), "订单初始状态应为待支付");

        // 2. 取消订单
        orderContext.cancelOrder();
        assertInstanceOf(CancelledState.class, orderContext.getCurrentState(), "待支付状态下取消，状态应变为已取消");
    }

    @Test
    @DisplayName("测试非法状态转换：在待支付状态下尝试发货")
    void testInvalidOperation_ShipWhenPendingPayment() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState(), "订单初始状态应为待支付");

        // 2. 尝试发货
        orderContext.shipOrder();
        // 3. 验证状态未改变
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState(), "待支付状态下发货失败，状态应保持不变");
    }

    @Test
    @DisplayName("测试非法状态转换：在已支付状态下重复支付")
    void testInvalidOperation_PayWhenPaid() {
        // 1. 先支付，进入“已支付”状态
        orderContext.payOrder();
        assertInstanceOf(PaidState.class, orderContext.getCurrentState(), "支付后状态应变为已支付");

        // 2. 再次尝试支付
        orderContext.payOrder();
        // 3. 验证状态未改变
        assertInstanceOf(PaidState.class, orderContext.getCurrentState(), "已支付状态下重复支付失败，状态应保持不变");
    }

    @Test
    @DisplayName("测试终结状态：订单完成后所有操作均无效")
    void testOperationsOnCompletedState() {
        // 走完完整流程
        orderContext.payOrder();
        orderContext.shipOrder();
        orderContext.confirmReceipt();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "订单状态应为已完成");

        // 在已完成状态下尝试所有操作
        orderContext.payOrder();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "已完成后支付，状态应保持不变");

        orderContext.shipOrder();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "已完成后发货，状态应保持不变");

        orderContext.confirmReceipt();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "已完成后确认收货，状态应保持不变");

        orderContext.cancelOrder();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState(), "已完成后取消，状态应保持不变");
    }
}