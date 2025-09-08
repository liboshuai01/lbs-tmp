package com.liboshuai.demo.factory.static_factory;

public class CoffeeStore {
    public String product(String type) {
        Coffee coffee = CoffeeSimpleFactory.createCoffee(type);
        return coffee.getName();
    }
}
