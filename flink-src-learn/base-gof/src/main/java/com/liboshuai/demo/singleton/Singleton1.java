package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 饿汉式: 静态变量形式
 */
public class Singleton1 implements Serializable {

    public static final Singleton1 INSTANCE = new Singleton1();

    private Singleton1() {

    }

    public Object readResolve() {
        return INSTANCE;
    }
}
