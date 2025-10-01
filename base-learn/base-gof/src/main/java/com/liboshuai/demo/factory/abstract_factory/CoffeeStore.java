package com.liboshuai.demo.factory.abstract_factory;

import com.liboshuai.demo.factory.abstract_factory.factory.DessertFactory;

public class CoffeeStore {
    private final DessertFactory dessertFactory;

    public CoffeeStore(DessertFactory dessertFactory) {
        this.dessertFactory = dessertFactory;
    }

    public String product() {
        return dessertFactory.createCoffee().getName() + "-" + dessertFactory.createDessert().getName();
    }
}
