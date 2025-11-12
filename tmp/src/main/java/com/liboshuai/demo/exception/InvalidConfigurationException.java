package com.liboshuai.demo.exception;

import java.io.Serial;

public class InvalidConfigurationException extends MyProjectException{

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
