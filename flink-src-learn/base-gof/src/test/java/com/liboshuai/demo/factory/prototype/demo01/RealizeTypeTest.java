package com.liboshuai.demo.factory.prototype.demo01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RealizeType 类的单元测试
 */
@DisplayName("原型模式：RealizeType类测试")
class RealizeTypeTest {

    private RealizeType realizeType;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，都创建一个新的原型实例
        // 这确保了测试之间的隔离性
        realizeType = new RealizeType();
    }

    @Test
    @DisplayName("测试原型对象克隆成功")
    void testClone_ShouldReturn_NewInstance() throws CloneNotSupportedException {
        // 1. 执行(Act)
        // 调用clone方法创建新对象
        RealizeType clonedObject = realizeType.clone();

        // 2.断言(Assert)
        // 验证克隆出的对象不是null
        assertNotNull(clonedObject, "克隆后的对象不应为null");
    }

    @Test
    @DisplayName("测试克隆对象与原型对象地址不同")
    void testClone_ShouldBe_DifferentObjects() throws CloneNotSupportedException {
        // 1. 执行(Act)
        RealizeType clonedObject = realizeType.clone();

        // 2. 断言(Assert)
        // 验证原型对象和克隆对象的引用地址不同
        // assertNotSame比较的是对象的内存地址
        assertNotSame(realizeType, clonedObject, "原型对象和克隆对象不应该是同一个对象");
    }

    @Test
    @DisplayName("测试克隆对象与原型对象的类型相同")
    void testClone_ShouldHave_SameClass() throws CloneNotSupportedException {
        // 1. 执行(Act)
        RealizeType clonedObject = realizeType.clone();

        // 2. 断言(Assert)
        // 验证原型对象和克隆对象的类是相同的
        assertEquals(realizeType.getClass(), clonedObject.getClass(), "原型对象和克隆对象的类型应该是相同的");
    }

    @Test
    @DisplayName("连续克隆多个对象")
    void testMultipleClones_Should_Succeed() throws CloneNotSupportedException {
        // 1. 执行(Act)
        RealizeType clone1 = realizeType.clone();
        RealizeType clone2 = realizeType.clone();

        // 2. 断言(Assert)
        assertNotNull(clone1, "第一个克隆对象不应为null");
        assertNotNull(clone2, "第二个克隆对象不应为null");
        assertNotSame(clone1, clone2, "两个克隆出的对象实例不应该是相同的");
        assertNotSame(realizeType, clone1, "原型对象和第一个克隆对象不应相同");
        assertNotSame(realizeType, clone2, "原型对象和第二个克隆对象不应相同");
    }
}