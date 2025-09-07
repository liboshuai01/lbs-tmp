package com.liboshuai.demo.demo03.after;

import com.liboshuai.demo.demo03.after.impl.IntelCpu;
import com.liboshuai.demo.demo03.after.impl.KingstonMemory;
import com.liboshuai.demo.demo03.after.impl.XiJieHardDisk;
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