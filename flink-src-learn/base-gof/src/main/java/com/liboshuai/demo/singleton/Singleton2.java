package com.liboshuai.demo.singleton;

public class Singleton2 {

    public static final Singleton2 INSTANCE;

    static {
        INSTANCE = new Singleton2();
    }

    private Singleton2() {

    }
}
