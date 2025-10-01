package com.liboshuai.demo.chain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Request {
    private final boolean login;
    private final boolean permission;
    private final String requestBody;
    private Map<String, Boolean> handleResutMap = new HashMap<>();

    public Request(boolean login, boolean permission, String requestBody) {
        this.login = login;
        this.permission = permission;
        this.requestBody = requestBody;
    }
}
