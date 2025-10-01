package com.liboshuai.demo.factory.simple_factory;

public class CoffeeStore {
    public String product(String type) {
        CoffeeSimpleFactory factory = new CoffeeSimpleFactory();
        Coffee coffee = factory.createCoffee(type);
        return coffee.getName();
    }
}
