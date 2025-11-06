package com.liboshuai.demo.function;


@FunctionalInterface
public interface RunnableWithException extends ThrowingRunnable<Exception> {

    @Override
    void run() throws Exception;
}
