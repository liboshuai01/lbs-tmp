package com.liboshuai.demo.mailbox;

/**
 * 一个允许抛出异常的 Runnable 接口。
 * 这是 Mailbox 模型的基础执行单元。
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;
}
