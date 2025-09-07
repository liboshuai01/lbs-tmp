package com.liboshuai.demo.demo03.before;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Computer {
    private IntelCpu cpu;
    private KingstonMemory memory;
    private XiJieHardDisk hardDisk;

    public String run() {
        String cpuResult = cpu.run();
        String memoryResult = memory.run();
        String diskResult = hardDisk.run();
        return String.format("%s-%s-%s", cpuResult, memoryResult, diskResult);
    }
}
