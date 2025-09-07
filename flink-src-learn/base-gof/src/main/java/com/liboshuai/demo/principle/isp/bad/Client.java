package com.liboshuai.demo.principle.isp.bad;

/**
 * 一个臃肿的、多功能的接口
 * 它包含了打印、扫描、传真等多种功能。
 */
interface IMultiFunctionDevice {
    void print();
    void scan();
    void fax();
}

/**
 * 高端的多功能一体机，它可以实现接口中的所有方法。
 */
class AllInOnePrinter implements IMultiFunctionDevice {
    @Override
    public void print() {
        System.out.println("打印文件...");
    }

    @Override
    public void scan() {
        System.out.println("扫描文档...");
    }

    @Override
    public void fax() {
        System.out.println("发送传真...");
    }
}

/**
 * 经济型打印机，它只应该有打印功能。
 * 但是，由于它实现了 IMultiFunctionDevice 接口，它被迫要对 scan() 和 fax() 方法提供实现。
 * 这就是“接口污染”。
 */
class EconomicPrinter implements IMultiFunctionDevice {
    @Override
    public void print() {
        System.out.println("打印文件...");
    }

    @Override
    public void scan() {
        // 空实现，因为它没有这个功能。
        // 或者更糟：
        throw new UnsupportedOperationException("这台打印机不支持扫描功能！");
    }

    @Override
    public void fax() {
        // 空实现，因为它没有这个功能。
        throw new UnsupportedOperationException("这台打印机不支持传真功能！");
    }
}

public class Client {
    public static void main(String[] args) {
        // 对于一体机，一切正常
        IMultiFunctionDevice advancedDevice = new AllInOnePrinter();
        advancedDevice.print();
        advancedDevice.scan();

        System.out.println("--------------------");

        // 对于经济型打印机，代码看起来很奇怪且危险
        IMultiFunctionDevice cheapDevice = new EconomicPrinter();
        cheapDevice.print(); // 正常工作
        // 下面这行代码在编译时是合法的，但运行时会抛出异常，非常危险！
        // cheapDevice.scan();
    }
}