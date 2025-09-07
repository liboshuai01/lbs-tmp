package com.liboshuai.demo.singleton;

import java.io.Serializable;

/**
 * 饿汉式：线程安全版本
 */
public class Singleton4 implements Serializable {
    private Singleton4() {}

    private static Singleton4 instance;

    // 多线程竞争激烈的环境下，性能低下（每个线程来都要获取锁、释放锁）
    public static synchronized Singleton4 getInstance() {
        if (instance == null) {
            instance = new Singleton4();
        }
        return instance;
    }

    public Object readResolve() {
        return getInstance();
    }
}
