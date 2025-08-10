package com.liboshuai.demo.safe.singleton;

/**
 * 懒汉式单例模式（线程安全 - 双重检查锁定版）
 * 优点：线程安全，且性能较高，实现了懒加载
 * 这是推荐的懒汉式单例模式实现之一
 */
public class LazySingletonDCL {

    private LazySingletonDCL() {

    }

    /**
     * 使用 volatile 关键字确保 instance 在多线程环境下的可见性和有序性
     */
    private static volatile LazySingletonDCL instance = null;

    public static LazySingletonDCL getInstance() {
        // 第一次检查：如果实例已经存在，则直接返回，无需进入同步代码块，提高性能
        if (instance == null) {
            // 同步代码块：只有在实例未创建时才进入
            synchronized (LazySingletonDCL.class) {
                // 第二次检查：防止多个线程同时通过第一次检查后，重复创建实例
                if (instance == null) {
                    instance = new LazySingletonDCL();
                }
            }
        }
        return instance;
    }
}
