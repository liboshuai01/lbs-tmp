package com.liboshuai.demo;

import java.io.Serializable;

/**
 * 单例模式
 */
public final class Test21 implements Serializable {

    public static void main(String[] args) {
        Singleton instance = Singleton.getInstance();
        System.out.println("instance: " + instance);
    }


    /**
     * 1.饿汉单例
     */
    static final class Singleton implements Serializable{

        private Singleton() {

        }

        private static final Singleton INSTANCE = new Singleton();

        public static Singleton getInstance() {
            return INSTANCE;
        }

        public Object readResolve() {
            return INSTANCE;
        }
    }

}
