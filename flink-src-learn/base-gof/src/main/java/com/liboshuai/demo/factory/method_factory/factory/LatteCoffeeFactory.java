package com.liboshuai.demo.factory.method_factory.factory;

import com.liboshuai.demo.factory.method_factory.coffee.Coffee;
import com.liboshuai.demo.factory.method_factory.coffee.LatteCoffee;

public class LatteCoffeeFactory implements CoffeeFactory {
    @Override
    public Coffee createCoffee() {
        return new LatteCoffee();
    }
}
