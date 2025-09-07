package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 懒汉式：静态内部类方式
 */
public class Singleton6 implements Serializable {
    private Singleton6() {}

    // 静态内部类只有在使用是才会被jvm加载，所以是懒汉式
    private static class Singleton5Holder {
        private static final Singleton6 INSTANCE = new Singleton6();
    }

    public static Singleton6 getInstance() {
        return Singleton5Holder.INSTANCE;
    }

    public Object readResolve() {
        return getInstance();
    }
}
