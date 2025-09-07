package com.liboshuai.demo.singleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Singleton4Test {
    @Test
    void test() {
        Singleton4 instance1 = Singleton4.INSTANCE;
        Singleton4 instance2 = Singleton4.INSTANCE;

        assertNotNull(instance1, "单例实例不应该为 null");
        assertNotNull(instance2, "单例实例不应该为 null");

        assertSame(instance1, instance2, "多次获取的实例应该是同一个对象");
    }
}