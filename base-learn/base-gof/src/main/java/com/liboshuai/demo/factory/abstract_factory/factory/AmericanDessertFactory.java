package com.liboshuai.demo.factory.abstract_factory.factory;

import com.liboshuai.demo.factory.abstract_factory.coffee.AmericanCoffee;
import com.liboshuai.demo.factory.abstract_factory.coffee.Coffee;
import com.liboshuai.demo.factory.abstract_factory.dessert.Cheesecake;
import com.liboshuai.demo.factory.abstract_factory.dessert.Dessert;

public class AmericanDessertFactory implements DessertFactory {
    @Override
    public Coffee createCoffee() {
        return new AmericanCoffee();
    }

    @Override
    public Dessert createDessert() {
        return new Cheesecake();
    }
}
