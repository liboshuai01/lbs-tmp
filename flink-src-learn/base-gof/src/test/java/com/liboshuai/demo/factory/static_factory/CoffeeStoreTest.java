package com.liboshuai.demo.factory.static_factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoffeeStoreTest {
    @Test
    void testLatteCoffee() {
        CoffeeStore coffeeStore = new CoffeeStore();
        String coffeeName = coffeeStore.product("latte");
        assertEquals("latteCoffee", coffeeName, "没有获取到正确的Coffee");
    }

    @Test
    void testLatteAmerican() {
        CoffeeStore coffeeStore = new CoffeeStore();
        String coffeeName = coffeeStore.product("american");
        assertEquals("americanCoffee", coffeeName, "没有获取到正确的Coffee");
    }
}