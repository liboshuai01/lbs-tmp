package com.liboshuai.demo.chain;

public abstract class AbstractHandler {

    private AbstractHandler next;

    public AbstractHandler setNext(AbstractHandler next) {
        this.next = next;
        return next;
    }

    public void handle(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数体为空！");
        }
        if (doHandle(request) && next != null) {
            next.handle(request);
        }
    }

    public abstract boolean doHandle(Request request);
}
