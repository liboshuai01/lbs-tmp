package com.liboshuai.demo.exception;

import java.io.Serial;

public class MyProjectRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MyProjectRuntimeException(String message) {
        super(message);
    }

    public MyProjectRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
