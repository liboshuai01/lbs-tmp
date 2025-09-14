package com.liboshuai.demo.chain;

public class BusinessLogicHandler extends AbstractHandler {
    @Override
    protected boolean doHandle(Request request) {
        request.addLog("4. [BusinessLogicHandler]: 所有检查通过，开始执行核心业务逻辑...");
        // ... 核心业务代码 ...
        request.addLog("-> 核心业务逻辑执行完毕。");
        return true; // 流程结束
    }
}
