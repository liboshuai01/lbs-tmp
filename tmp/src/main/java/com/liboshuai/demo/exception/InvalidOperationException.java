package com.liboshuai.demo.exception;

import java.io.Serial;

public class InvalidOperationException extends MyProjectRuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
