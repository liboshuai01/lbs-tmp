package com.liboshuai.demo.function;

@FunctionalInterface
public interface ConsumerWithException<T, E extends Throwable> {
    void accept(T t) throws E;
}
