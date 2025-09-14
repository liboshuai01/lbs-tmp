package com.liboshuai.demo.chain;

public class ValidationHandler extends AbstractHandler {
    @Override
    protected boolean doHandle(Request request) {
        request.addLog("3. [ValidationHandler]: 正在校验参数...");
        String requestBody = request.getRequestBody();
        if (requestBody == null || requestBody.trim().isEmpty()) {
            request.addLog("-> 参数校验失败，请求体为空！请求被拦截。");
            return false; // 中断链
        }
        request.addLog("-> 参数验证成功。");
        return true; // 继续下一个处理
    }
}
