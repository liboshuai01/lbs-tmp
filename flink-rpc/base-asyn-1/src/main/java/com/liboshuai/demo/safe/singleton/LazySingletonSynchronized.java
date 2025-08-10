package com.liboshuai.demo.safe.singleton;

/**
 * 懒汉式单例模式（线程安全 - 同步方法版）
 * 优点：解决了多线程安全问题
 * 缺点：性能低下。每次调用 getInstance() 都会进行同步，但实际上只有第一次创建实例时才需要同步
 */
public class LazySingletonSynchronized {

    private LazySingletonSynchronized() {

    }

    private static LazySingletonSynchronized instance = null;

    public static synchronized LazySingletonSynchronized getStance() {
        if (instance == null) {
            instance = new LazySingletonSynchronized();
        }
        return instance;
    }

    /**
     * 示例方法
     */
    public void showMessage() {
        System.out.println("这是一个懒汉式单例！");
    }
}
