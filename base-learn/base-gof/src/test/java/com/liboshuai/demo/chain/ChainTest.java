package com.liboshuai.demo.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChainTest {

    private AbstractHandler chain;

    @BeforeEach
    void setUp() {
        // 构建责任链
        chain = new AuthenticationHandler();
        chain.setNext(new AuthorizationHandler())
                .setNext(new ValidationHandler())
                .setNext(new BusinessLogicHandler());
    }

    @Test
    @DisplayName("成功路径: 请求通过所有处理器")
    void should_Pass_All_Handlers_When_Request_Is_Valid() {
        // GIVEN: 一个完全合法的请求
        Request request = new Request(true, true, "Valid Request Body");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查处理日志，确认所有处理器都被执行
        Map<String, Boolean> handleResutMap = request.getHandleResutMap();
        assertTrue(handleResutMap.get("authentication"));
        assertTrue(handleResutMap.get("authorization"));
        assertTrue(handleResutMap.get("validation"));
        assertTrue(handleResutMap.get("businessLogic"));
    }

    @Test
    @DisplayName("失败路径: 请求在认证环节被拦截")
    void should_Be_Intercepted_At_Authentication_When_Not_Logged_In() {
        // GIVEN: 一个未登录的请求
        Request request = new Request(false, true, "Valid Request Body");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在认证处中断
        Map<String, Boolean> handleResutMap = request.getHandleResutMap();
        assertFalse(handleResutMap.get("authentication"));
        assertNull(handleResutMap.get("authorization"));
        assertNull(handleResutMap.get("validation"));
        assertNull(handleResutMap.get("businessLogic"));
    }

    @Test
    @DisplayName("失败路径: 请求在授权环节被拦截")
    void should_Be_Intercepted_At_Authorization_When_No_Permission() {
        // GIVEN: 一个已登录但无权限的请求
        Request request = new Request(true, false, "Valid Request Body");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在授权处中断
        Map<String, Boolean> handleResutMap = request.getHandleResutMap();
        assertTrue(handleResutMap.get("authentication"));
        assertFalse(handleResutMap.get("authorization"));
        assertNull(handleResutMap.get("validation"));
        assertNull(handleResutMap.get("businessLogic"));
    }

    @Test
    @DisplayName("失败路径: 请求在参数校验环节被拦截")
    void should_Be_Intercepted_At_Validation_When_Body_Is_Empty() {
        // GIVEN: 一个通过认证和授权但参数无效的请求
        Request request = new Request(true, true, "");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在校验处中断
        Map<String, Boolean> handleResutMap = request.getHandleResutMap();
        assertTrue(handleResutMap.get("authentication"));
        assertTrue(handleResutMap.get("authorization"));
        assertFalse(handleResutMap.get("validation"));
        assertNull(handleResutMap.get("businessLogic"));
    }
}