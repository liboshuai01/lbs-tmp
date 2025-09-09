package com.liboshuai.demo.budiler;

import lombok.Data;

@Data
public final class Computer { // 使用 final 关键字，使其成为不可变类
    // 必需属性
    private final String cpu; // CPU
    private final String ram; // 内存
    private final String storage; // 硬盘

    // 可选属性
    private final String gpu; // 显卡
    private final String monitor; // 显示器

    // 关键点1：构造函数是私有的，并且接收一个 Builder 对象
    // 这强制外部必须通过 Builder 来创建 Computer 实例
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.ram = builder.ram;
        this.storage = builder.storage;
        this.gpu = builder.gpu;
        this.monitor = builder.monitor;
    }

    // 关键点2：提供一个静态内部类 Builder
    public static class Builder {
        // 必需属性
        private final String cpu; // CPU
        private final String ram; // 内存
        private final String storage; // 硬盘

        // 可选属性
        private String gpu = "集成显卡"; // 显卡
        private String monitor = "无"; // 显示器

        // 关键点3：Builder 的构造函数只接收必需的属性
        public Builder(String cpu, String ram, String storage) {
            this.cpu = cpu;
            this.ram = ram;
            this.storage = storage;
        }

        // 关键点4：为可选属性提供 "wither" 方法
        // 每个方法都返回 Builder 自身，以实现链式调用（Fluent API）
        public Builder gpu(String gpu) {
            this.gpu = gpu;
            return this; // 返回 this 是实现链式调用的核心
        }

        public Builder monitor(String monitor) {
            this.monitor = monitor;
            return this; // 返回 this
        }

        // 关键点5：提供一个 build() 方法
        // 这个方法负责创建最终的 Computer 实例
        // 可以在这里添加参数效验逻辑
        public Computer build() {
            if (cpu == null || ram == null || storage == null) {
                throw new IllegalStateException("CPU, RAM, Storage 是必需的组件！");
            }
            return new Computer(this);
        }
    }
}
