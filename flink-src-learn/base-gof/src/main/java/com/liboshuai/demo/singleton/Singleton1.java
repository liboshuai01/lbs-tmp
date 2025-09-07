package com.liboshuai.demo.singleton;

/**
 * 饿汉式: 静态变量形式
 */
@SuppressWarnings("InstantiationOfUtilityClass")
public class Singleton1 {

    public static final Singleton1 INSTANCE = new Singleton1();

    private Singleton1() {

    }
}
