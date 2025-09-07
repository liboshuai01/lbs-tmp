package com.liboshuai.demo.singleton;

import java.io.Serializable;

public class Singleton5 implements Serializable {
    private Singleton5() {}

    private static class Singleton5Holder {
        private static final Singleton5 INSTANCE = new Singleton5();
    }

    public static Singleton5 getInstance() {
        return Singleton5Holder.INSTANCE;
    }

    public Object readResolve() {
        return getInstance();
    }
}
