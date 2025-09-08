package com.liboshuai.demo.factory.abstract_factory;

import com.liboshuai.demo.factory.abstract_factory.factory.AmericanDessertFactory;
import com.liboshuai.demo.factory.abstract_factory.factory.ItalicanDessertFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoffeeStoreTest {
    @Test
    void testLatteCoffee() {
        CoffeeStore coffeeStore = new CoffeeStore(new ItalicanDessertFactory());
        String coffeeName = coffeeStore.product();
        assertEquals("latteCoffee-tiramisu", coffeeName, "没有获取到正确的Coffee");
    }

    @Test
    void testLatteAmerican() {
        CoffeeStore coffeeStore = new CoffeeStore(new AmericanDessertFactory());
        String coffeeName = coffeeStore.product();
        assertEquals("americanCoffee-cheesecake", coffeeName, "没有获取到正确的Coffee");
    }
}