package com.liboshuai.demo.function;


@FunctionalInterface
public interface FunctionWithException<T, R, E extends Throwable> {

    R apply(T value) throws E;
}
