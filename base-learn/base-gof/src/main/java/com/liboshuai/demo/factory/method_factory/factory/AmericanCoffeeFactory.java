package com.liboshuai.demo.factory.method_factory.factory;

import com.liboshuai.demo.factory.method_factory.coffee.AmericanCoffee;
import com.liboshuai.demo.factory.method_factory.coffee.Coffee;

public class AmericanCoffeeFactory implements CoffeeFactory {
    @Override
    public Coffee createCoffee() {
        return new AmericanCoffee();
    }
}
