package com.liboshuai.demo.exception;

import java.io.Serial;

public class MyProjectException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public MyProjectException(String message) {
        super(message);
    }

    public MyProjectException(String message, Throwable cause) {
        super(message, cause);
    }

}
