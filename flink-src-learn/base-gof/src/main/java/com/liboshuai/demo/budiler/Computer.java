package com.liboshuai.demo.budiler;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class Computer {

    private final String cpu;
    private final String ram;
    private final String storage;

    private final String gpu;
    private final String monitor;

    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.ram = builder.ram;
        this.storage = builder.storage;
        this.gpu = builder.gpu;
        this.monitor = builder.monitor;
    }

    public static class Builder {
        private final String cpu;
        private final String ram;
        private final String storage;

        private String gpu;
        private String monitor;

        public Builder(String cpu, String ram, String storage) {
            this.cpu = cpu;
            this.ram = ram;
            this.storage = storage;
        }

        public Builder gpu(String gpu) {
            this.gpu = gpu;
            return this;
        }

        public Builder monitor(String monitor) {
            this.monitor = monitor;
            return this;
        }

        public Computer build() {
            if (cpu == null | ram == null | storage == null) {
                throw new IllegalStateException("cpu、memory、storage是必需组件");
            }
            return new Computer(this);
        }
    }
}
