package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 懒汉式：double-check线程安全版本
 */
public class Singleton5 implements Serializable {

    // 一定要加 volatile，可见性提高了性能，禁止重排序防止获取到未初始化完成的对象
    private static volatile Singleton5 instance;

    private Singleton5() {}

    // 性能提高很高，除了第一次要竞争锁，后面都不需要
    public static Singleton5 getInstance() {
        if (instance == null) {
            synchronized (Singleton5.class) {
                if (instance == null) {
                    instance = new Singleton5();
                }
            }
        }
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }

}
