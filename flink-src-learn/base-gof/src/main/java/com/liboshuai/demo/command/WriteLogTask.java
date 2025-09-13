package com.liboshuai.demo.command;

public class WriteLogTask implements Task {

    private final LoggingService loggingService;
    private final String level;
    private final String message;

    public WriteLogTask(LoggingService loggingService, String level, String message) {
        this.loggingService = loggingService;
        this.level = level;
        this.message = message;
    }

    @Override
    public void execute() {
        this.loggingService.log(level, message);
    }
}
