package com.liboshuai.demo.function;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Utility class for Flink's functions. */
public class FunctionUtils {

    private FunctionUtils() {
        throw new UnsupportedOperationException("This class should never be instantiated.");
    }


    public static <A, B> Function<A, B> uncheckedFunction(
            FunctionWithException<A, B, ?> functionWithException) {
        return (A value) -> {
            try {
                return functionWithException.apply(value);
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
                // we need this to appease the compiler :-(
                return null;
            }
        };
    }

    public static <A> Consumer<A> uncheckedConsumer(ThrowingConsumer<A, ?> throwingConsumer) {
        return (A value) -> {
            try {
                throwingConsumer.accept(value);
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
            }
        };
    }


    public static <T> Supplier<T> uncheckedSupplier(
            SupplierWithException<T, ?> supplierWithException) {
        return () -> {
            T result = null;
            try {
                result = supplierWithException.get();
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
            }
            return result;
        };
    }

    public static <T> Callable<T> asCallable(RunnableWithException command, T result) {
        return () -> {
            command.run();
            return result;
        };
    }
}
