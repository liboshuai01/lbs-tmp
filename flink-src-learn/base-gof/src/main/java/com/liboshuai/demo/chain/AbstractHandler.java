package com.liboshuai.demo.chain;

/**
 * 抽象处理者
 */
public abstract class AbstractHandler {
    protected AbstractHandler next; // 指向链中的下一个处理者

    /**
     * 设置一个处理者
     */
    public AbstractHandler setNext(AbstractHandler next) {
        this.next = next;
        return next;
    }

    /**
     * 处理请求的模板方法
     */
    public final void handle(Request request) {
        // 如果当前处理器处理成功，则交由下一个处理器处理
        if (doHandle(request)) {
            if (next != null) {
                next.handle(request);
            }
        }
        // 如果当前处理器处理失败，则流程中断
    }

    /**
     * 具体的处理逻辑，由子类实现
     */
    protected abstract boolean doHandle(Request request);
}
