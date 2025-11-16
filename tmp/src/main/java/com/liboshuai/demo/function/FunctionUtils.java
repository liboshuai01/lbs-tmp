package com.liboshuai.demo.function;

import com.liboshuai.demo.exception.ExceptionUtils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FunctionUtils {

    private FunctionUtils() {
        throw new UnsupportedOperationException("This class should never be instantiated.");
    }

    public static <T, R> Function<T, R> uncheckedFunction(FunctionWithException<T, R, ?> functionWithException) {
        return (T value) -> {
            try {
                return functionWithException.apply(value);
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
                return null;
            }
        };
    }

    public static <R> Supplier<R> uncheckedSupplier(SupplierWithException<R, ?> supplierWithException) {
        return () -> {
            try {
                return supplierWithException.get();
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
                return null;
            }
        };
    }

    public static <T> Consumer<T> uncheckedConsumer(ConsumerWithException<T, ?> consumerWithException) {
        return (T value) -> {
            try {
                consumerWithException.accept(value);
            } catch (Throwable t) {
                ExceptionUtils.rethrow(t);
            }
        };
    }

    public static Runnable uncheckedRunnable(RunnableWithException<?> runnableWithException) {
        return () -> {
          try {
              runnableWithException.run();
          }catch (Throwable t){
              ExceptionUtils.rethrow(t);
          }
        };
    }
}
