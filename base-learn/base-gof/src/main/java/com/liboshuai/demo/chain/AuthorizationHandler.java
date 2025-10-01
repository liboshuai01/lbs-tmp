package com.liboshuai.demo.chain;

public class AuthorizationHandler extends AbstractHandler {
    @Override
    public boolean doHandle(Request request) {
        if (request.isPermission()) {
            request.getHandleResutMap().put("authorization", true);
            return true;
        }
        request.getHandleResutMap().put("authorization", false);
        return false;
    }
}
