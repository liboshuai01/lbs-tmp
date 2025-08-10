package com.liboshuai.demo.safe.singleton;

/**
 * 懒汉式单例模式（基础版本）
 * 优点：实现了懒加载，只有在需要时才创建实例，节约了资源
 * 缺点：在多线程环境下存在安全问题
 */
public class LazySingleton {
    /**
     * 同样的私有化构造函数，防止外部通过new来实例化对象
     */
    private LazySingleton() {

    }

    /**
     * 声明一个静态实例，但是不立即初始化
     */
    private static LazySingleton instance = null;

    /**
     * 提供一个公共的静态方法，用于外部获取唯一的静态实例
     */
    public static LazySingleton getInstace() {
        // 在这里，多线程环境下会出现问题
        if (instance == null) {
            instance = new LazySingleton();
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
