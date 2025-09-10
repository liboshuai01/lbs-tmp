package com.liboshuai.demo.flyweight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShapeFactoryTest {

    @Test
    void test() {
        Shape redCircle = ShapeFactory.getShape("red");
        String result1 = redCircle.draw(3, 5);
        assertEquals("圆形，颜色：red，坐标：(3, 5)", result1);
        assertEquals(1, ShapeFactory.getMapSize());

        Shape blueCircle = ShapeFactory.getShape("blue");
        String result2 = blueCircle.draw(4, 6);
        assertEquals("圆形，颜色：blue，坐标：(4, 6)", result2);
        assertEquals(2, ShapeFactory.getMapSize());

        Shape redCircle2 = ShapeFactory.getShape("red");
        String result3 = redCircle2.draw(5, 7);
        assertEquals("圆形，颜色：red，坐标：(5, 7)", result3);
        assertEquals(2, ShapeFactory.getMapSize());
        assertSame(redCircle, redCircle2);
    }
}