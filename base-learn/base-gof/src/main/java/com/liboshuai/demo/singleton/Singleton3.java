package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 懒汉式：线程不安全版本
 */
public class Singleton3 implements Serializable {
    private Singleton3() {}

    private static Singleton3 instance;

    public static Singleton3 getInstance() {
        if (instance == null) { // 可能同时有多个线程进入if语句，导致重复创建不同的对象
            instance = new Singleton3();
        }
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }
}
