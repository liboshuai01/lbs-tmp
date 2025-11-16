package com.liboshuai.demo.exception;

public final class ExceptionUtils {
    private ExceptionUtils() {
        throw new UnsupportedOperationException("This class should never be instantiated.");
    }

    public static void rethrow(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new RuntimeException(t);
        }
    }
}
