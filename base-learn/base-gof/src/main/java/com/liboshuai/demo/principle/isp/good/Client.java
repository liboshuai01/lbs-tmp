package com.liboshuai.demo.principle.isp.good;


// 步骤 1: 将大接口拆分成多个职责单一的小接口
interface IPrintable {
    void print();
}

interface IScannable {
    void scan();
}

interface IFaxable {
    void fax();
}

/**
 * 步骤 2: 类根据自身能力，按需实现接口
 * 高端的多功能一体机，它实现了所有接口。
 */
class AllInOnePrinter implements IPrintable, IScannable, IFaxable {
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
 * 经济型打印机，它只需要实现 IPrintable 接口。
 * 它不再需要知道 scan 或 fax 的存在，代码非常干净。
 */
class EconomicPrinter implements IPrintable {
    @Override
    public void print() {
        System.out.println("打印文件...");
    }
}

public class Client {
    // 步骤 3: 客户端的方法依赖于最小的必须接口

    /**
     * 这个方法只需要打印功能，所以它的参数是 IPrintable。
     * 这样，任何实现了 IPrintable 接口的对象都可以传进来。
     */
    public static void executePrintTask(IPrintable device) {
        System.out.println("执行打印任务...");
        device.print();
    }

    /**
     * 这个方法需要扫描功能，所以参数是 IScannable。
     */
    public static void executeScanTask(IScannable device) {
        System.out.println("执行扫描任务...");
        device.scan();
    }

    public static void main(String[] args) {
        // 创建设备实例
        IPrintable advancedPrinter = new AllInOnePrinter();
        IPrintable cheapPrinter = new EconomicPrinter();

        // 我们可以安全地将它们都用于打印任务
        executePrintTask(advancedPrinter);
        executePrintTask(cheapPrinter);

        System.out.println("--------------------");

        // 只有多功能一体机可以用于扫描任务
        IScannable advancedScanner = new AllInOnePrinter();
        executeScanTask(advancedScanner);

        // 关键点：下面这行代码在编译时就会直接报错！
        // 因为 EconomicPrinter 类型没有实现 IScannable 接口。
        // executeScanTask(cheapPrinter);
        // 这样就从根本上杜绝了运行时错误。
    }
}