package com.liboshuai.demo.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("订单状态模式测试")
class OrderContextTest {

    private OrderContext orderContext;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，都创建一个新的订单
        orderContext = new OrderContext(2025091511401110123L);
    }

    @Test
    @DisplayName("测试订单完整生命周期：创建 -> 支付 -> 发货 -> 确认收货 -> 完成")
    void testOrderFullLifecycle() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
        // 2. 支付订单
        orderContext.pay();
        assertInstanceOf(PaidState.class, orderContext.getCurrentState());
        // 3. 发货
        orderContext.ship();
        assertInstanceOf(ShippedState.class, orderContext.getCurrentState());
        // 4. 确认收货
        orderContext.confirm();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState());
    }

    @Test
    @DisplayName("测试订单创建后直接取消")
    void testOrderCancellation() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
        // 2. 取消订单
        orderContext.cancel();
        assertInstanceOf(CancelledState.class, orderContext.getCurrentState());
    }

    @Test
    @DisplayName("测试非法状态转换：在待支付状态下尝试发货")
    void testInvalidOperation_ShipWhenPendingPayment() {
        // 1. 初始状态检查
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
        // 2. 尝试发货
        orderContext.ship();
        // 3. 验证状态未改变
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
    }

    @Test
    @DisplayName("测试非法状态转换：在已支付状态下重复支付")
    void testInvalidOperation_PayWhenPaid() {
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
        // 1. 先支付，进入“已支付”状态
        orderContext.pay();
        // 2. 再次尝试支付
        orderContext.pay();
        // 3. 验证状态未改变
        assertInstanceOf(PaidState.class, orderContext.getCurrentState());
    }

    @Test
    @DisplayName("测试终结状态：订单完成后所有操作均无效")
    void testOperationsOnCompletedState() {
        assertInstanceOf(PendingPaymentState.class, orderContext.getCurrentState());
        // 走完完整流程
        orderContext.pay();
        orderContext.ship();
        orderContext.confirm();
        // 在已完成状态下尝试所有操作
        orderContext.cancel();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState());

        orderContext.pay();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState());

        orderContext.ship();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState());

        orderContext.confirm();
        assertInstanceOf(CompletedState.class, orderContext.getCurrentState());
    }
}