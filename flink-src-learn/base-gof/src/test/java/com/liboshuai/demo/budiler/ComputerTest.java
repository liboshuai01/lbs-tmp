package com.liboshuai.demo.budiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ComputerTest {

    @Test
    void test1() {
        Computer computer = new Computer.Builder(
                "Intel Core i9-13900K",
                "DDR5 32GB",
                "2TB NVMe SSD"
        )
                .gpu("NVIDIA GeForce RTX 4090")
                .monitor("4K 144Hz 电竞屏")
                .build();
        assertNotNull(computer);
        assertEquals("Intel Core i9-13900K", computer.getCpu());
        assertEquals("DDR5 32GB", computer.getRam());
        assertEquals("2TB NVMe SSD", computer.getStorage());
        assertEquals("NVIDIA GeForce RTX 4090", computer.getGpu());
        assertEquals("4K 144Hz 电竞屏", computer.getMonitor());
    }

    @Test
    void test2() {
        Computer computer = new Computer.Builder(
                "Intel Core i5-13400",
                "DDR4 16GB",
                "1TB SATA SSD"
        ).build();
        assertNotNull(computer);
        assertEquals("Intel Core i5-13400", computer.getCpu());
        assertEquals("DDR4 16GB", computer.getRam());
        assertEquals("1TB SATA SSD", computer.getStorage());
        assertNull(computer.getGpu());
        assertNull(computer.getMonitor());
    }
  
}