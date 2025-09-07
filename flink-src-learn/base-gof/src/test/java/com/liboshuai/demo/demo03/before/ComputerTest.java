package com.liboshuai.demo.demo03.before;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputerTest {

    @Test
    void run() {
        Computer computer = new Computer();
        computer.setCpu(new IntelCpu());
        computer.setMemory(new KingstonMemory());
        computer.setHardDisk(new XiJieHardDisk());
        String result = computer.run();
        assertEquals("Cpu-Memory-Disk", result);
    }
}