package com.liboshuai.demo.chain;

public class AuthorizationHandler extends AbstractHandler {
    @Override
    protected boolean doHandle(Request request) {
        request.addLog("2. [AuthorizationHandler]: 正在检查授权...");
        if (!request.isHasPermission()) {
            request.addLog("-> 授权失败，用户无权限！请求被拦截。");
            return false; // 中断链
        }
        request.addLog("-> 授权成功。");
        return true; // 继续下一个处理
    }
}
