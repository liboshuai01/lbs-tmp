package com.liboshuai.demo.factory.method_factory;

import com.liboshuai.demo.factory.method_factory.factory.AmericanCoffeeFactory;
import com.liboshuai.demo.factory.method_factory.factory.LatteCoffeeFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoffeeStoreTest {
    @Test
    void testLatteCoffee() {
        CoffeeStore coffeeStore = new CoffeeStore(new LatteCoffeeFactory());
        String coffeeName = coffeeStore.product();
        assertEquals("latteCoffee", coffeeName, "没有获取到正确的Coffee");
    }

    @Test
    void testLatteAmerican() {
        CoffeeStore coffeeStore = new CoffeeStore(new AmericanCoffeeFactory());
        String coffeeName = coffeeStore.product();
        assertEquals("americanCoffee", coffeeName, "没有获取到正确的Coffee");
    }
}