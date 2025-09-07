package com.liboshuai.demo.singleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Singleton1Test {

    @Test
    void test() {
        Singleton1 instance1 = Singleton1.INSTANCE;
        Singleton1 instance2 = Singleton1.INSTANCE;

        assertNotNull(instance1, "单例实例不应该为 null");
        assertNotNull(instance2, "单例实例不应该为 null");

        assertSame(instance1, instance2, "多次获取的实例应该是同一个对象");
    }

}