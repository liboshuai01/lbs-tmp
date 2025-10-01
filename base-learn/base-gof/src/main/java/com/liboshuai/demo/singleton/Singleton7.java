package com.liboshuai.demo.singleton;

// 最完美的饿汉式的单例模式（相比于普通的饿汉式，更加简洁，jvm保证线程安全，没有序列化问题，对反射有天津防御能力）
public enum Singleton7 {
    INSTANCE
}
