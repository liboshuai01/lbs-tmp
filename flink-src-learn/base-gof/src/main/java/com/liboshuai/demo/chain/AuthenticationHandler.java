package com.liboshuai.demo.chain;

public class AuthenticationHandler extends AbstractHandler {
    @Override
    protected boolean doHandle(Request request) {
        request.addLog("1. [AuthenticationHandler]: 正在检查认证...");
        if (!request.isLoggedIn()) {
            request.addLog("-> 认证失败，用户未登录！请求被拦截。");
            return false;
        }
        request.addLog("-> 认证成功。");
        return true; // 继续下一个处理
    }
}
