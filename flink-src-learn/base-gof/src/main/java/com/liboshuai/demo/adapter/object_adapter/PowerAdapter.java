package com.liboshuai.demo.adapter.object_adapter;

/**
 * Adapter: 对象适配器
 * 1. 实现了目标接口 DC100V。
 * 2. 内部持有一个被适配者 AC220V 的实例（组合关系）。
 */
public class PowerAdapter implements DC100V {

    // 关键：不再是继承，而是持有被适配者的引用
    private final AC220V ac220v;

    /**
     * 通过构造函数传入被适配者的实例
     * @param ac220v 被适配的对象
     */
    public PowerAdapter(AC220V ac220v) {
        this.ac220v = ac220v;
    }

    @Override
    public int output100V() {
        // 1. 从持有的被适配者实例中获取220V输入电压
        int inputVoltage = this.ac220v.output220V();

        // 2. 实现具体的转换逻辑（与类适配器相同）
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
        // 转换逻辑与之前完全一样
        return (inputVoltage / 2) - 10;
    }
}