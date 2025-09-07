package com.liboshuai.demo.singleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 饿汉式: 静态代码块形式
 */
class Singleton2Test {

    @Test
    void test() {
        Singleton2 instance1 = Singleton2.INSTANCE;
        Singleton2 instance2 = Singleton2.INSTANCE;

        assertNotNull(instance1, "单例实例不应该为 null");
        assertNotNull(instance2, "单例实例不应该为 null");

        assertSame(instance1, instance2, "多次获取的实例应该是同一个对象");
    }

}