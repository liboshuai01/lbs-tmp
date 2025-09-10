package com.liboshuai.demo.bridge.shape;

import com.liboshuai.demo.bridge.color.BlueColor;
import com.liboshuai.demo.bridge.color.Color;
import com.liboshuai.demo.bridge.color.RedColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("桥接模式测试")
class ShapeTest {

    @Test
    @DisplayName("测试绘制红色圆形")
    void testDrawRedCircle() {
        // Arrange: 创建一个具体的实现者（红色）
        Color redColor = new RedColor();
        // Arrange: 创建一个扩展的抽象部分（圆形），并通过构造函数将实现者“桥接”进去
        Shape redCircle = new Circle(redColor);
        // Act: 调用方法
        String result = redCircle.draw();
        // Assert: 验证结果是否是抽象部分和实现部分正确组合的结果
        assertEquals("绘制圆形，并涂上红色", result, "红色圆形绘制不正确");
    }

    @Test
    @DisplayName("测试绘制蓝色正方形")
    void testDrawBlueSquare() {
        // Arrange: 创建一个具体的实现者（蓝色）
        Color blueColor = new BlueColor();
        // Arrange: 创建一个扩展的抽象部分（正方形），并桥接蓝色实现
        Shape blueSquare = new Square(blueColor);
        // Act
        String result = blueSquare.draw();
        // Assert
        assertEquals("绘制正方形，并涂上蓝色", result, "蓝色正方形绘制不正确");
    }

    @Test
    @DisplayName("测试将蓝色圆形（一种组合）")
    void testDrawBlueCircle() {
        // Arrange
        Color blueColor = new BlueColor();
        Shape blueCircle = new Circle(blueColor);
        // Act
        String result = blueCircle.draw();
        // Assert
        assertEquals("绘制圆形，并涂上蓝色", result, "蓝色圆形绘制不正确");
    }

    @Test
    @DisplayName("测试将红色正方形（另一种组合）")
    void testDrawRedSquare() {
        // Arrange
        Color redColor = new RedColor();
        Shape redSquare = new Square(redColor);
        // Act
        String result = redSquare.draw();
        // Assert
        assertEquals("绘制正方形，并涂上红色", result, "红色正方形绘制不正确");
    }
}