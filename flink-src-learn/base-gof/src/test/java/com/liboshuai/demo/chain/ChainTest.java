package com.liboshuai.demo.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        List<String> logs = request.getProcessingLog();
        assertTrue(logs.contains("-> 认证成功。"));
        assertTrue(logs.contains("-> 授权成功。"));
        assertTrue(logs.contains("-> 参数验证成功。"));
        assertTrue(logs.contains("-> 核心业务逻辑执行完毕。"));
    }

    @Test
    @DisplayName("失败路径: 请求在认证环节被拦截")
    void should_Be_Intercepted_At_Authentication_When_Not_Logged_In() {
        // GIVEN: 一个未登录的请求
        Request request = new Request(false, true, "Valid Request Body");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在认证处中断
        List<String> logs = request.getProcessingLog();
        assertTrue(logs.contains("-> 认证失败，用户未登录！请求被拦截。"));
        assertFalse(logs.contains("2. [AuthorizationHandler]: 正在检查授权...")); // 确认后续处理器未执行
        assertFalse(logs.contains("-> 核心业务逻辑执行完毕。"));
    }

    @Test
    @DisplayName("失败路径: 请求在授权环节被拦截")
    void should_Be_Intercepted_At_Authorization_When_No_Permission() {
        // GIVEN: 一个已登录但无权限的请求
        Request request = new Request(true, false, "Valid Request Body");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在授权处中断
        List<String> logs = request.getProcessingLog();
        assertTrue(logs.contains("-> 认证成功。"));
        assertTrue(logs.contains("-> 授权失败，用户无权限！请求被拦截。"));
        assertFalse(logs.contains("3. [ValidationHandler]: 正在校验参数...")); // 确认后续处理器未执行
    }

    @Test
    @DisplayName("失败路径: 请求在参数校验环节被拦截")
    void should_Be_Intercepted_At_Validation_When_Body_Is_Empty() {
        // GIVEN: 一个通过认证和授权但参数无效的请求
        Request request = new Request(true, true, "");

        // WHEN: 责任链处理请求
        chain.handle(request);

        // THEN: 检查日志，确认流程在校验处中断
        List<String> logs = request.getProcessingLog();
        assertTrue(logs.contains("-> 授权成功。"));
        assertTrue(logs.contains("-> 参数校验失败，请求体为空！请求被拦截。"));
        assertFalse(logs.contains("4. [BusinessLogicHandler]: 所有检查通过，开始执行核心业务逻辑...")); // 确认后续处理器未执行
    }
}