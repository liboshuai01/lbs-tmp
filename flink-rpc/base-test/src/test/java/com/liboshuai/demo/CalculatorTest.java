package com.liboshuai.demo;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    @Test
    void shouldReturnSumWhenTwoNumbersAreAdded() {
        // Arrange (准备)
        Calculator calculator = new Calculator();
        int a = 10;
        int b = 5;

        // Act (执行)
        int result = calculator.add(a, b);

        // Assert (断言)
        assertThat(result).isEqualTo(15);
    }

    @Test
    void shouldReturnQuotientWhenDividing() {
        // Arrange
        Calculator calculator = new Calculator();

        // Act
        double result = calculator.divide(10, 4);

        // Assert
        assertThat(result).isEqualTo(2.5);
    }

    @Test
    void shouldThrowExceptionWhenDividingByZero() {
        // Arrange
        Calculator calculator = new Calculator();

        // Act & Assert (对于异常测试，执行和断言通常合并)
        // 验证当执行 divide(1, 0) 时，会抛出 IllegalArgumentException 异常
        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.divide(1, 0)
        );

        // 还可以对抛出的异常信息进行断言
        assertThat(thrownException.getMessage()).isEqualTo("Divisor cannot be zero");
    }
}