package com.liboshuai.demo.demo03.after;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Computer {
    private Cpu cpu;
    private Memory memory;
    private HardDisk hardDisk;

    public String run() {
        String cpuResult = cpu.run();
        String memoryResult = memory.run();
        String diskResult = hardDisk.run();
        return String.format("%s-%s-%s", cpuResult, memoryResult, diskResult);
    }
}
