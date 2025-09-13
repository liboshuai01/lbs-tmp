package com.liboshuai.demo.command;

public class SaveFileTask implements Task {

    private final FileIOService fileIOService;
    private final String fileName;

    public SaveFileTask(FileIOService fileIOService, String fileName) {
        this.fileIOService = fileIOService;
        this.fileName = fileName;
    }

    @Override
    public void execute() {
        this.fileIOService.saveFile(fileName);
    }
}
