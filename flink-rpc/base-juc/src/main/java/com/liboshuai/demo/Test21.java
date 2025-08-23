package com.liboshuai.demo;

import java.io.Serializable;

/**
 * 单例模式
 */
public final class Test21 {

    /**
     * 1.饿汉单例
     */
    static final class Singleton1 implements Serializable{

        private Singleton1() {

        }

        private static final Singleton1 INSTANCE = new Singleton1();

        public static Singleton1 getInstance() {
            return INSTANCE;
        }

        public Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * 2. 懒汉式
     */
    static final class Singleton2 implements Serializable{
        private Singleton2() {

        }

        private static Singleton2 INSTANCE;

        public static synchronized Singleton2 getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new Singleton2();
            }
            return INSTANCE;
        }

        public Object readResolve() {
            return getInstance();
        }
    }

    /**
     * 3. DCL 懒汉式
     */
    static final class Singleton3 implements Serializable{
        private Singleton3() {

        }

        private static volatile Singleton3 INSTANCE;

        public static Singleton3 getInstance() {
            if (INSTANCE == null) {
                synchronized (Singleton3.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new Singleton3();
                    }
                }
            }
            return INSTANCE;
        }

        public Object readResolve() {
            return getInstance();
        }
    }

    /**
     * 4. 枚举单例
     */
    static enum Singleton4 {
        INSTANCE;
    }

    /**
     * 5. 静态内部类懒汉单例
     */
    static final class Singleton5 implements Serializable{
        private Singleton5() {

        }

        private static final class LazyHolder {
            static final Singleton5 INSTANCE = new Singleton5();
        }

        public static Singleton5 getInstance() {
            return LazyHolder.INSTANCE;
        }

        public Object readResolve() {
            return getInstance();
        }
    }

}
