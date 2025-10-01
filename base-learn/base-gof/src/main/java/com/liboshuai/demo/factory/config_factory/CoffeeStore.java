package com.liboshuai.demo.factory.config_factory;

import com.liboshuai.demo.factory.config_factory.coffee.Coffee;
import com.liboshuai.demo.factory.config_factory.factory.CoffeeSimpleFactory;

public class CoffeeStore {

    public String product(String type) {
        CoffeeSimpleFactory factory = new CoffeeSimpleFactory();
        Coffee coffee = factory.createCoffee(type);
        return coffee.getName();
    }
}
