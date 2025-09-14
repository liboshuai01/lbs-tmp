package com.liboshuai.demo.chain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求对象，它将在责任链中被传递
 */
public class Request {
    @Getter
    private final boolean loggedIn;
    @Getter
    private final boolean hasPermission;
    @Getter
    private final String requestBody;

    // 用于记录处理日志，方便单元测试断言
    @Getter
    private final List<String> processingLog = new ArrayList<>();

    public Request(boolean loggedIn, boolean hasPermission, String requestBody) {
        this.loggedIn = loggedIn;
        this.hasPermission = hasPermission;
        this.requestBody = requestBody;
    }

    public void addLog(String logEntry) {
        this.processingLog.add(logEntry);
    }

}
