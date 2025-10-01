package com.liboshuai.demo.decorator.beverage;

import com.liboshuai.demo.decorator.condiment.Milk;
import com.liboshuai.demo.decorator.condiment.Mocha;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("饮品装饰者模式测试")
class BeverageTest {

    @Test
    @DisplayName("测试基础浓缩咖啡的价格和描述")
    void testEspresso() {
        Espresso espresso = new Espresso();
        assertEquals("浓缩咖啡", espresso.getDescription());
        assertEquals(new BigDecimal("3"), espresso.getPrice());
    }

    @Test
    @DisplayName("测试一杯加了牛奶的浓缩咖啡")
    void testEspressoWithMilk() {
        // 用牛奶装饰浓缩咖啡
        Milk milk = new Milk(new Espresso());
        assertEquals("浓缩咖啡，加牛奶", milk.getDescription());
        assertEquals(new BigDecimal("3.5"), milk.getPrice());
    }

    @Test
    @DisplayName("测试一杯加了摩卡的美式咖啡")
    void testLongBlackWithMocha() {
        // 用摩卡装饰美式咖啡
        Mocha mocha = new Mocha(new LongBlack());
        assertEquals("美式咖啡，加摩卡", mocha.getDescription());
        assertEquals(new BigDecimal("5.3"), mocha.getPrice());
    }

    @Test
    @DisplayName("测试一杯双份摩卡加牛奶的美式咖啡")
    void testComplexBeverage() {
        // 这是一个复杂的组合：先是美式咖啡，然后加一份摩卡，再加一份牛奶，最后再加一份摩卡
        Mocha mocha = new Mocha(new Milk(new Mocha(new LongBlack())));
        assertEquals("美式咖啡，加摩卡，加牛奶，加摩卡", mocha.getDescription());
        assertEquals(new BigDecimal("6.1"), mocha.getPrice());
    }
}