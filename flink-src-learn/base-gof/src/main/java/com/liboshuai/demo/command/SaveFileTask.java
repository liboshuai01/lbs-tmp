package com.liboshuai.demo.command;

public class SaveFileTask implements Task {

    private final FileIOService fileService;
    private final String fileName;

    public SaveFileTask(FileIOService fileService, String fileName) {
        this.fileService = fileService;
        this.fileName = fileName;
    }

    @Override
    public void execute() {
        this.fileService.saveFile(fileName);
    }
}
