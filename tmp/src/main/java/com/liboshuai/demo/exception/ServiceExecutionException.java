package com.liboshuai.demo.exception;

import java.io.Serial;

public class ServiceExecutionException extends MyProjectRuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    private final String serviceId;

    public ServiceExecutionException(String serviceId, String message) {
        super(message);
        this.serviceId = serviceId;
    }

    public ServiceExecutionException(String serviceId, String message, Throwable cause) {
        super(message, cause);
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }
}
