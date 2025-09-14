package com.liboshuai.demo.chain;

public class BusinessLogicHandler extends AbstractHandler {
    @Override
    public boolean doHandle(Request request) {
        request.getHandleResutMap().put("businessLogic", true);
        return true;
    }
}
