package com.liboshuai.demo.chain;

public class ValidationHandler extends AbstractHandler {
    @Override
    public boolean doHandle(Request request) {
        String requestBody = request.getRequestBody();
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            request.getHandleResutMap().put("validation", true);
            return true;
        }
        request.getHandleResutMap().put("validation", false);
        return false;
    }
}
