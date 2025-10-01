package com.liboshuai.demo.factory.method_factory;

import com.liboshuai.demo.factory.method_factory.factory.CoffeeFactory;

public class CoffeeStore {
    private final CoffeeFactory coffeeFactory;

    public CoffeeStore(CoffeeFactory coffeeFactory) {
        this.coffeeFactory = coffeeFactory;
    }

    public String product() {
        return coffeeFactory.createCoffee().getName();
    }
}
