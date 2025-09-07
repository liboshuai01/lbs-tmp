package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 懒汉式：double-check线程安全版本
 */
public class Singleton3 implements Serializable {

    private static volatile Singleton3 instance;

    private Singleton3() {}

    public static Singleton3 getInstance() {
        if (instance == null) {
            synchronized (Singleton3.class) {
                if (instance == null) {
                    instance = new Singleton3();
                }
            }
        }
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }

}
