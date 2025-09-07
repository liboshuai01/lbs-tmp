package com.liboshuai.demo.singleton;

@SuppressWarnings("InstantiationOfUtilityClass")
public class Singleton5 {
    private Singleton5() {}

    private static class Singleton5Holder {
        private static final Singleton5 INSTANCE = new Singleton5();
    }

    public static Singleton5 getInstance() {
        return Singleton5Holder.INSTANCE;
    }
}
