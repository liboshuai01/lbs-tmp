package com.liboshuai.demo.singleton;

import java.io.Serializable;

public class Singleton2 implements Serializable {

    public static final Singleton2 INSTANCE;

    static {
        INSTANCE = new Singleton2();
    }

    private Singleton2() {

    }

    public Object readResolve() {
        return INSTANCE;
    }
}
