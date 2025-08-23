package com.liboshuai.demo;

import java.io.Serializable;

/**
 * 单例模式
 */
public final class Test21 implements Serializable {

    public static void main(String[] args) {
        Singleton1 instance = Singleton1.getInstance();
        System.out.println("instance: " + instance);
    }


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


}
