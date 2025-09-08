package com.liboshuai.demo.factory.normal_factory.factory;

import com.liboshuai.demo.factory.normal_factory.coffee.AmericanCoffee;
import com.liboshuai.demo.factory.normal_factory.coffee.Coffee;

public class AmericanCoffeeFactory implements CoffeeFactory {
    @Override
    public Coffee createCoffee() {
        return new AmericanCoffee();
    }
}
