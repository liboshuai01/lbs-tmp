package com.liboshuai.demo.chain;

public class AuthenticationHandler extends AbstractHandler {
    @Override
    public boolean doHandle(Request request) {
        if (request.isLogin()) {
            request.getHandleResutMap().put("authentication", true);
            return true;
        }
        request.getHandleResutMap().put("authentication", false);
        return false;
    }
}
