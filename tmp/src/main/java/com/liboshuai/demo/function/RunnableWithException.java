package com.liboshuai.demo.function;

@FunctionalInterface
public interface RunnableWithException<E extends Throwable> {
    void run() throws E;
}
