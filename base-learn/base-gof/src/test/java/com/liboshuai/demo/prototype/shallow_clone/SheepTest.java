package com.liboshuai.demo.prototype.shallow_clone;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SheepTest {

    @Test
    void test1() {
        Sheep oldSheep = new Sheep("多利", new Date());
        Sheep newSheep = null;
        try {
            newSheep = oldSheep.clone();
        } catch (CloneNotSupportedException e) {
            fail("克隆失败，原因：", e);
        }
        assertNotSame(oldSheep, newSheep, "原对象与克隆对象应该为两个不同的对象");
        assertEquals(oldSheep.getClass(), newSheep.getClass(), "原对象与克隆对象应该属于同一个类");
        assertEquals(oldSheep.getName(), newSheep.getName(), "原对象与克隆对象的name属性值应该相同");
        assertEquals(oldSheep.getBirthday(), newSheep.getBirthday(), "原对象与克隆对象的birthday属性值应该相同");
    }

    @Test
    void test2() {
        Sheep oldSheep = new Sheep("多利", new Date());
        Sheep newSheep = null;
        try {
            newSheep = oldSheep.clone();
        } catch (CloneNotSupportedException e) {
            fail("克隆失败，原因：", e);
        }
        newSheep.setName("克隆多利");
        newSheep.setBirthday(new Date(0L));
        assertNotSame(oldSheep, newSheep, "原对象与克隆对象应该为两个不同的对象");
        assertEquals(oldSheep.getClass(), newSheep.getClass(), "原对象与克隆对象应该属于同一个类");
        assertNotEquals(oldSheep.getName(), newSheep.getName(), "原对象与克隆对象的name属性值应该不同");
        assertNotEquals(oldSheep.getBirthday(), newSheep.getBirthday(), "原对象与克隆对象的birthday属性值应该不同");
    }

    @Test
    void test3() {
        Date birthday = new Date();
        Sheep oldSheep = new Sheep("多利", birthday);
        Sheep newSheep = null;
        try {
            newSheep = oldSheep.clone();
        } catch (CloneNotSupportedException e) {
            fail("克隆失败，原因：", e);
        }
        newSheep.setName("克隆多利");
        birthday.setTime(0L);
        assertNotSame(oldSheep, newSheep, "原对象与克隆对象应该为两个不同的对象");
        assertEquals(oldSheep.getClass(), newSheep.getClass(), "原对象与克隆对象应该属于同一个类");
        assertNotEquals(oldSheep.getName(), newSheep.getName(), "原对象与克隆对象的name属性值应该不同");
        assertEquals(oldSheep.getBirthday(), newSheep.getBirthday(), "原对象与克隆对象的birthday属性值应该相同");
    }

}