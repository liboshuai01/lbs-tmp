package com.liboshuai.demo.function;

@FunctionalInterface
public interface SupplierWithException<R, E extends Throwable> {
    R get() throws E;
}
