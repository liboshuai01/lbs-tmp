package com.liboshuai.demo.adapter.class_adapter;

/**
 * Adapter: 类适配器
 * 继承 AC220V，实现 DC100V 接口
 */
public class PowerAdapter extends AC220V implements DC100V {

    @Override
    public int output100V() {
        // 1. 调用父类方法，获取220V输入电压
        int inputVoltage = super.output220V();

        // 2. 实现具体的转换逻辑
        System.out.println("--- [适配器] 开始工作，输入电压: " + inputVoltage + "V ---");
        int outputVoltage = performConversion(inputVoltage);
        System.out.println("--- [适配器] 转换完成，输出电压: " + outputVoltage + "V ---");

        // 3. 返回符合目标接口的值
        return outputVoltage;
    }

    /**
     * 模拟电压转换的复杂逻辑
     * @param inputVoltage 输入电压
     * @return 转换后的电压
     */
    private int performConversion(int inputVoltage) {
        // 假设转换逻辑是：电压 / 2 再减去 10
        // 220V -> 220 / 2 = 110 -> 110 - 10 = 100V
        // 这是一个可测试的纯函数
        return (inputVoltage / 2) - 10;
    }
}